import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

suspend fun fetchNotes(): List<dataclass> {
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val notesRef: DatabaseReference = database.getReference("notes").child(myGlobalMobileDeviceId)

    return try {
        Log.d("fetchNotes", "Fetching notes for device ID: $myGlobalMobileDeviceId")
        val query = notesRef.orderByChild("mymobiledeviceid").equalTo(myGlobalMobileDeviceId)
        val snapshot = query.get().await()

        if (snapshot.exists()) {
            val notes = snapshot.children.mapNotNull { it.getValue(dataclass::class.java) }
            Log.d("fetchNotes", "Found ${notes.size} notes")

            // Process each note - decrypt if encrypted, return as-is if not
            val processedNotes = notes.map { note ->
                Log.d("fetchNotes", "Processing note: ${note.id}")
                Log.d("fetchNotes", "Title preview: ${note.title.take(20)}...")
                Log.d("fetchNotes", "Description preview: ${note.description.take(20)}...")

                // Try to decrypt the note
                val decryptedNote = dataclass.fromEncryptedData(note)

                Log.d("fetchNotes", "After processing - Title: ${decryptedNote.title.take(20)}...")
                Log.d("fetchNotes", "After processing - Description: ${decryptedNote.description.take(20)}...")

                decryptedNote
            }

            Log.d("fetchNotes", "Successfully processed ${processedNotes.size} notes")
            processedNotes
        } else {
            Log.d("fetchNotes", "No notes found")
            emptyList()
        }
    } catch (e: Exception) {
        Log.e("fetchNotes", "Error fetching notes from Firebase", e)
        emptyList()
    }
}
