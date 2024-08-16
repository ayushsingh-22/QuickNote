import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

suspend fun fetchNotes(): List<dataclass> {
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val notesRef: DatabaseReference = database.getReference("notes").child(myGlobalMobileDeviceId)

    return try {

        val query = notesRef.orderByChild("mymobiledeviceid").equalTo(myGlobalMobileDeviceId)
        val snapshot = query.get().await()
        if (snapshot.exists()) {
            println(snapshot.children.mapNotNull { it.getValue(dataclass::class.java) })
            snapshot.children.mapNotNull { it.getValue(dataclass::class.java) }
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        Log.e("fetchNotes", "Error fetching notes from Firebase", e)
        emptyList()
    }
}


