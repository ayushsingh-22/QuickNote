import android.util.Log
import com.amvarpvtltd.selfnote.repository.NoteRepository

private val noteRepository = NoteRepository()

suspend fun fetchNotes(): List<dataclass> {
    return try {
        val result = noteRepository.fetchNotes()
        if (result.isSuccess) {
            result.getOrNull() ?: emptyList()
        } else {
            Log.e("fetchNotes", "Error fetching notes: ${result.exceptionOrNull()?.message}")
            emptyList()
        }
    } catch (e: Exception) {
        Log.e("fetchNotes", "Error fetching notes", e)
        emptyList()
    }
}
