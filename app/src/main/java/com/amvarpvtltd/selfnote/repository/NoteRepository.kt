package com.amvarpvtltd.selfnote.repository

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import dataclass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import myGlobalMobileDeviceId
import com.amvarpvtltd.selfnote.offline.OfflineNoteManager
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val context: Context? = null) {
    private val database = FirebaseDatabase.getInstance()
    private val notesRef: DatabaseReference get() = database.getReference("notes").child(myGlobalMobileDeviceId)

    // Offline manager - initialized when context is available
    private val offlineManager: OfflineNoteManager? = context?.let { OfflineNoteManager(it) }

    companion object {
        private const val TAG = "NoteRepository"
    }

    /**
     * Save a new note or update an existing one with offline support
     */
    suspend fun saveNote(
        title: String,
        description: String,
        noteId: String? = null,
        context: Context
    ): Result<String> {
        val note = dataclass(title = title.trim(), description = description.trim())
        note.mymobiledeviceid = myGlobalMobileDeviceId
        if (noteId != null) {
            note.id = noteId
        }

        // Always save offline first for immediate response
        val offlineManager = OfflineNoteManager(context)
        val offlineResult = offlineManager.saveNoteOffline(note)

        if (offlineResult.isFailure) {
            return offlineResult
        }

        // Try to save online
        return try {
            val encryptedNote = note.toEncryptedData()
            notesRef.child(note.id).setValue(encryptedNote).await()
            Log.d(TAG, "Note saved successfully online: ${note.id}")
            Result.success(note.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save note online: ${e.message}", e)
            // Note is already saved offline, so this is still a success from user perspective
            Result.success(note.id)
        }
    }

    /**
     * Load a note by ID with offline fallback
     */
    suspend fun loadNote(noteId: String, context: Context? = null): Result<dataclass> {
        // First try offline storage if context is available
        context?.let { ctx ->
            val offlineManager = OfflineNoteManager(ctx)
            val offlineNote = offlineManager.getNoteById(noteId)
            if (offlineNote != null) {
                Log.d(TAG, "Note loaded from offline storage: $noteId")
                return Result.success(offlineNote)
            }
        }

        // Then try online storage
        return try {
            val snapshot = notesRef.child(noteId).get().await()
            if (snapshot.exists()) {
                val encryptedNote = snapshot.getValue(dataclass::class.java)
                if (encryptedNote != null) {
                    val decryptedNote = dataclass.fromEncryptedData(encryptedNote)
                    Log.d(TAG, "Note loaded successfully from online: $noteId")
                    Result.success(decryptedNote)
                } else {
                    Log.e(TAG, "Note data is null for ID: $noteId")
                    Result.failure(Exception("Note data is null"))
                }
            } else {
                Log.e(TAG, "Note not found online: $noteId")
                Result.failure(Exception("Note not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load note online: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Delete a note by ID
     */
    suspend fun deleteNote(noteId: String, context: Context): Result<String> {
        // Always delete offline first
        val offlineManager = OfflineNoteManager(context)
        val offlineResult = offlineManager.deleteNoteOffline(noteId)

        if (offlineResult.isFailure) {
            return offlineResult
        }

        // Try to delete online
        return try {
            notesRef.child(noteId).removeValue().await()
            Log.d(TAG, "Note deleted successfully online: $noteId")
            Result.success(noteId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete note online: ${e.message}", e)
            // Note is already deleted offline, so this is still a success from user perspective
            Result.success(noteId)
        }
    }

    /**
     * Get all notes with offline support
     */
    suspend fun getAllNotes(): Result<List<dataclass>> {
        return try {
            val snapshot = notesRef.get().await()
            val notes = mutableListOf<dataclass>()

            snapshot.children.forEach { childSnapshot ->
                try {
                    val encryptedNote = childSnapshot.getValue(dataclass::class.java)
                    if (encryptedNote != null) {
                        val decryptedNote = dataclass.fromEncryptedData(encryptedNote)
                        notes.add(decryptedNote)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error decrypting note: ${childSnapshot.key}", e)
                }
            }

            Log.d(TAG, "Loaded ${notes.size} notes from Firebase")
            Result.success(notes)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get notes: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch notes with offline fallback
     */
    suspend fun fetchNotes(): Result<List<dataclass>> {
        return try {
            // Try to get online notes first
            val onlineResult = getAllNotes()
            if (onlineResult.isSuccess) {
                return onlineResult
            }

            // Fallback to offline notes if online fails
            context?.let { ctx ->
                val offlineManager = OfflineNoteManager(ctx)
                val offlineNotes = offlineManager.offlineNotes.value
                Log.d(TAG, "Using offline notes: ${offlineNotes.size}")
                Result.success(offlineNotes)
            } ?: Result.failure(Exception("No context available for offline fallback"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch notes: ${e.message}", e)

            // Final fallback to offline
            context?.let { ctx ->
                val offlineManager = OfflineNoteManager(ctx)
                val offlineNotes = offlineManager.offlineNotes.value
                Result.success(offlineNotes)
            } ?: Result.failure(e)
        }
    }

    /**
     * Sync offline notes with Firebase
     */
    suspend fun syncOfflineNotes(context: Context): Result<String> {
        val offlineManager = OfflineNoteManager(context)
        val pendingNotes = offlineManager.pendingSyncNotes.value

        if (pendingNotes.isEmpty()) {
            return Result.success("No notes to sync")
        }

        return try {
            var syncedCount = 0
            pendingNotes.forEach { note ->
                try {
                    val encryptedNote = note.toEncryptedData()
                    notesRef.child(note.id).setValue(encryptedNote).await()
                    syncedCount++
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync note: ${note.id}", e)
                }
            }

            // Clear synced notes from pending list
            offlineManager.clearSyncedNotes()

            Log.d(TAG, "Synced $syncedCount out of ${pendingNotes.size} notes")
            Result.success("Synced $syncedCount notes")
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Check if a note exists
     */
    suspend fun noteExists(noteId: String): Boolean {
        return try {
            val snapshot = notesRef.child(noteId).get().await()
            snapshot.exists()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking note existence: ${e.message}", e)
            false
        }
    }

    /**
     * Check if there are pending sync operations
     */
    fun hasPendingSync(context: Context): Boolean {
        val offlineManager = OfflineNoteManager(context)
        return offlineManager.hasPendingSync()
    }
}
