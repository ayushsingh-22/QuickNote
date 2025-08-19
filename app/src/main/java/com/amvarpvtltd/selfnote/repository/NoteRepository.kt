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

class NoteRepository {
    private val database = FirebaseDatabase.getInstance()
    private val notesRef: DatabaseReference get() = database.getReference("notes").child(myGlobalMobileDeviceId)

    companion object {
        private const val TAG = "NoteRepository"
    }

    /**
     * Save a new note or update an existing one
     */
    suspend fun saveNote(
        title: String,
        description: String,
        noteId: String? = null,
        context: Context
    ): Result<String> {
        return try {
            val note = dataclass(title = title.trim(), description = description.trim())
            note.mymobiledeviceid = myGlobalMobileDeviceId

            val deviceIdRef = database.getReference("notes").child(myGlobalMobileDeviceId)

            if (noteId == null) {
                // Create new note
                val encryptedNote = note.toEncryptedData()
                deviceIdRef.push().setValue(encryptedNote).await()

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "✅ Note saved successfully!", Toast.LENGTH_SHORT).show()
                }

                Result.success("Note saved successfully")
            } else {
                // Update existing note
                val query = notesRef.orderByChild("id").equalTo(noteId)
                val keyToUpdate = query.get().await().children.firstOrNull()?.key

                if (keyToUpdate != null) {
                    note.id = noteId
                    val encryptedNote = note.toEncryptedData()
                    deviceIdRef.child(keyToUpdate).setValue(encryptedNote).await()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "✅ Note updated successfully!", Toast.LENGTH_SHORT).show()
                    }

                    Result.success("Note updated successfully")
                } else {
                    Result.failure(Exception("Note not found"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving note", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "❌ Error saving note", Toast.LENGTH_LONG).show()
            }
            Result.failure(e)
        }
    }

    /**
     * Delete a note by ID
     */
    suspend fun deleteNote(noteId: String, context: Context): Result<String> {
        return try {
            val query = notesRef.orderByChild("id").equalTo(noteId)
            val keyToDelete = query.get().await().children.firstOrNull()?.key

            if (keyToDelete != null) {
                notesRef.child(keyToDelete).removeValue().await()

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "🗑️ Note deleted successfully", Toast.LENGTH_SHORT).show()
                }

                Result.success("Note deleted successfully")
            } else {
                Result.failure(Exception("Note not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting note", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "❌ Error deleting note", Toast.LENGTH_LONG).show()
            }
            Result.failure(e)
        }
    }

    /**
     * Load a note by ID
     */
    suspend fun loadNote(noteId: String): Result<dataclass> {
        return try {
            Log.d(TAG, "Loading note with ID: $noteId")
            val query = notesRef.orderByChild("id").equalTo(noteId)
            val snapshot = query.get().await()

            if (snapshot.exists()) {
                val keyToUpdate = snapshot.children.first().key
                if (keyToUpdate != null) {
                    val noteSnapshot = notesRef.child(keyToUpdate).get().await()
                    val encryptedNote = noteSnapshot.getValue(dataclass::class.java)

                    if (encryptedNote != null) {
                        // Decrypt the note data before returning
                        val decryptedNote = dataclass.fromEncryptedData(encryptedNote)
                        Log.d(TAG, "Successfully loaded and decrypted note: $noteId")
                        Result.success(decryptedNote)
                    } else {
                        Result.failure(Exception("Note data is null"))
                    }
                } else {
                    Result.failure(Exception("Note key is null"))
                }
            } else {
                Result.failure(Exception("Note not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading note", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch all notes for the current device
     */
    suspend fun fetchNotes(): Result<List<dataclass>> {
        return try {
            Log.d(TAG, "Fetching notes for device ID: $myGlobalMobileDeviceId")
            val query = notesRef.orderByChild("mymobiledeviceid").equalTo(myGlobalMobileDeviceId)
            val snapshot = query.get().await()

            if (snapshot.exists()) {
                val notes = snapshot.children.mapNotNull { it.getValue(dataclass::class.java) }
                Log.d(TAG, "Found ${notes.size} notes")

                // Process each note - decrypt if encrypted, return as-is if not
                val processedNotes = notes.map { note ->
                    Log.d(TAG, "Processing note: ${note.id}")
                    // Try to decrypt the note
                    val decryptedNote = dataclass.fromEncryptedData(note)
                    decryptedNote
                }

                Log.d(TAG, "Successfully processed ${processedNotes.size} notes")
                Result.success(processedNotes)
            } else {
                Log.d(TAG, "No notes found")
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching notes from Firebase", e)
            Result.failure(e)
        }
    }
}
