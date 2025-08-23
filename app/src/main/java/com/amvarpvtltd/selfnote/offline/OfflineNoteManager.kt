package com.amvarpvtltd.selfnote.offline

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dataclass
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.amvarpvtltd.selfnote.utils.Constants

class OfflineNoteManager(private val context: Context) {
    private val gson = Gson()
    private val prefs = context.getSharedPreferences("offline_notes", Context.MODE_PRIVATE)

    private val _offlineNotes = MutableStateFlow<List<dataclass>>(emptyList())
    val offlineNotes: StateFlow<List<dataclass>> = _offlineNotes.asStateFlow()

    private val _pendingSyncNotes = MutableStateFlow<List<dataclass>>(emptyList())
    val pendingSyncNotes: StateFlow<List<dataclass>> = _pendingSyncNotes.asStateFlow()

    companion object {
        private const val TAG = "OfflineNoteManager"
    }

    init {
        loadOfflineNotes()
        loadPendingSyncNotes()
    }

    fun saveNoteOffline(note: dataclass): Result<String> {
        return try {
            val currentNotes = _offlineNotes.value.toMutableList()
            val existingIndex = currentNotes.indexOfFirst { it.id == note.id }

            val updatedNote = note.copy(
                // Add timestamp for offline tracking
            )

            if (existingIndex >= 0) {
                currentNotes[existingIndex] = updatedNote
            } else {
                currentNotes.add(0, updatedNote)
            }

            _offlineNotes.value = currentNotes
            saveOfflineNotesToPrefs(currentNotes)

            // Add to pending sync
            addToPendingSync(updatedNote)

            Log.d(TAG, "Note saved offline: ${note.id}")
            Result.success("Note saved offline successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving note offline", e)
            Result.failure(e)
        }
    }

    /**
     * Delete note offline
     */
    fun deleteNoteOffline(noteId: String): Result<String> {
        return try {
            val currentNotes = _offlineNotes.value.toMutableList()
            val noteToDelete = currentNotes.find { it.id == noteId }

            if (noteToDelete != null) {
                // Remove from offline notes immediately
                currentNotes.removeAll { it.id == noteId }
                _offlineNotes.value = currentNotes
                saveOfflineNotesToPrefs(currentNotes)

                Log.d(TAG, "Note deleted offline: $noteId")
                Result.success("Note deleted offline successfully")
            } else {
                Result.failure(Exception("Note not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting note offline", e)
            Result.failure(e)
        }
    }


    fun getNoteById(noteId: String): dataclass? {
        return _offlineNotes.value.find { it.id == noteId }
    }

    /**
     * Sync offline notes with Firebase when online
     */
    suspend fun syncWithFirebase(
        onlineSaveNote: suspend (dataclass) -> Result<String>,
        onlineDeleteNote: suspend (String) -> Result<String>,
        onlineFetchNotes: suspend () -> Result<List<dataclass>>
    ): Result<String> {
        return try {
            Log.d(TAG, "Starting sync with Firebase...")

            // First, fetch latest notes from Firebase
            val onlineResult = onlineFetchNotes()
            if (onlineResult.isSuccess) {
                val onlineNotes = onlineResult.getOrNull() ?: emptyList()
                mergeWithOnlineNotes(onlineNotes)
            }

            // Sync pending changes
            val pendingNotes = _pendingSyncNotes.value
            var syncSuccessCount = 0
            var syncErrorCount = 0

            for (note in pendingNotes) {
                try {
                    if (note.title == "__DELETED__" && note.description == "__DELETED__") {
                        // Handle deletion
                        val deleteResult = onlineDeleteNote(note.id)
                        if (deleteResult.isSuccess) {
                            syncSuccessCount++
                        } else {
                            syncErrorCount++
                        }
                    } else {
                        // Handle save/update
                        val saveResult = onlineSaveNote(note)
                        if (saveResult.isSuccess) {
                            syncSuccessCount++
                        } else {
                            syncErrorCount++
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error syncing note ${note.id}", e)
                    syncErrorCount++
                }
            }

            // Clear pending sync if all successful
            if (syncErrorCount == 0) {
                clearPendingSync()
                updateLastSyncTime()
                Log.d(TAG, "Sync completed successfully: $syncSuccessCount notes synced")
                Result.success("Sync completed: $syncSuccessCount notes synced")
            } else {
                Log.w(TAG, "Sync completed with errors: $syncSuccessCount successful, $syncErrorCount failed")
                Result.success("Partial sync: $syncSuccessCount successful, $syncErrorCount failed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during sync", e)
            Result.failure(e)
        }
    }

    /**
     * Check if there are pending changes to sync
     */
    fun hasPendingSync(): Boolean {
        return _pendingSyncNotes.value.isNotEmpty()
    }

    /**
     * Get last sync time
     */
    fun getLastSyncTime(): Long {
        return prefs.getLong(Constants.LAST_SYNC_TIME_KEY, 0L)
    }

    private fun loadOfflineNotes() {
        try {
            val notesJson = prefs.getString(Constants.OFFLINE_NOTES_KEY, "[]")
            val type = object : TypeToken<List<dataclass>>() {}.type
            val notes: List<dataclass> = gson.fromJson(notesJson, type) ?: emptyList()
            _offlineNotes.value = notes
            Log.d(TAG, "Loaded ${notes.size} offline notes")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading offline notes", e)
            _offlineNotes.value = emptyList()
        }
    }

    private fun loadPendingSyncNotes() {
        try {
            val pendingJson = prefs.getString(Constants.SYNC_PENDING_KEY, "[]")
            val type = object : TypeToken<List<dataclass>>() {}.type
            val pendingNotes: List<dataclass> = gson.fromJson(pendingJson, type) ?: emptyList()
            _pendingSyncNotes.value = pendingNotes
            Log.d(TAG, "Loaded ${pendingNotes.size} pending sync notes")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading pending sync notes", e)
            _pendingSyncNotes.value = emptyList()
        }
    }

    private fun saveOfflineNotesToPrefs(notes: List<dataclass>) {
        try {
            val notesJson = gson.toJson(notes)
            prefs.edit().putString(Constants.OFFLINE_NOTES_KEY, notesJson).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving offline notes to prefs", e)
        }
    }

    private fun addToPendingSync(note: dataclass) {
        try {
            val currentPending = _pendingSyncNotes.value.toMutableList()
            // Remove existing entry for same note ID
            currentPending.removeAll { it.id == note.id }
            // Add new entry
            currentPending.add(note)

            _pendingSyncNotes.value = currentPending

            val pendingJson = gson.toJson(currentPending)
            prefs.edit().putString(Constants.SYNC_PENDING_KEY, pendingJson).apply()

            Log.d(TAG, "Added note to pending sync: ${note.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding to pending sync", e)
        }
    }

//    private fun addDeletionToPendingSync(note: dataclass) {
//        try {
//            val currentPending = _pendingSyncNotes.value.toMutableList()
//
//            // Remove any existing entries for this note ID (in case it was modified before deletion)
//            currentPending.removeAll { it.id == note.id }
//
//            // Save only to pending sync preferences, not to offline notes
//            val pendingJson = gson.toJson(currentPending)
//            prefs.edit().putString(Constants.SYNC_PENDING_KEY, pendingJson).apply()
//
//            Log.d(TAG, "Added deletion marker to pending sync: ${note.id}")
//        } catch (e: Exception) {
//            Log.e(TAG, "Error adding deletion to pending sync", e)
//        }
//    }

    private fun clearPendingSync() {
        _pendingSyncNotes.value = emptyList()
        prefs.edit().putString(Constants.SYNC_PENDING_KEY, "[]").apply()
    }

    private fun updateLastSyncTime() {
        prefs.edit().putLong(Constants.LAST_SYNC_TIME_KEY, System.currentTimeMillis()).apply()
    }

    private fun mergeWithOnlineNotes(onlineNotes: List<dataclass>) {
        try {
            val offlineNotesMap = _offlineNotes.value.associateBy { it.id }.toMutableMap()
            val mergedNotes = mutableListOf<dataclass>()

            // Add online notes, preferring offline versions if they exist and are newer
            for (onlineNote in onlineNotes) {
                val offlineNote = offlineNotesMap[onlineNote.id]
                if (offlineNote != null) {
                    // Use offline version (assume it's more recent)
                    mergedNotes.add(offlineNote)
                    offlineNotesMap.remove(onlineNote.id)
                } else {
                    mergedNotes.add(onlineNote)
                }
            }

            // Add remaining offline-only notes
            mergedNotes.addAll(offlineNotesMap.values)

            _offlineNotes.value = mergedNotes.sortedByDescending {
                // Sort by creation time (newest first)
                it.id.hashCode() // Simple sorting, can be improved with timestamps
            }

            saveOfflineNotesToPrefs(_offlineNotes.value)
            Log.d(TAG, "Merged notes: ${mergedNotes.size} total")
        } catch (e: Exception) {
            Log.e(TAG, "Error merging with online notes", e)
        }
    }

    /**
     * Clear synced notes from pending list
     */
    fun clearSyncedNotes() {
        clearPendingSync()
        Log.d(TAG, "Cleared synced notes from pending list")
    }
}
