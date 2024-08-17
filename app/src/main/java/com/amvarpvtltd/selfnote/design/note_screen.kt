package com.amvarpvtltd.selfnote.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.amvarpvtltd.selfnote.ui.theme.Lobster_Font
import com.google.firebase.database.FirebaseDatabase
import dataclass
import fetchNotes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import myGlobalMobileDeviceId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(navController: NavHostController) {

   val notesState = remember { mutableStateOf<List<dataclass>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            notesState.value =  fetchNotes()
        }
    }

    Scaffold(topBar = {

        TopAppBar(modifier = Modifier.padding(top = 30.dp),
            title = {
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 60.sp,
                    fontFamily = Lobster_Font,
                    fontWeight = FontWeight.Bold,
                )
            },
        )
    }, content = { paddingValues ->

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color.Unspecified)
            ) {
                items(notesState.value) { note ->
                    NoteItem_DESIGN(note = note,
                        isLastData = notesState.value.last() == note,
                        onEdit = {
                        navController.navigate("addscreen/${note.id}")
                    }, onDelete = {
                        scope.launch(Dispatchers.IO) {
                            try {
                                println("Attempting to delete note with ID: ${note.id}")

                                val database = FirebaseDatabase.getInstance()
                                val notesRef = database.getReference("notes").child(myGlobalMobileDeviceId)

                                // Query to find the note by its ID
                                val query = notesRef.orderByChild("id").equalTo(note.id)
                                val snapshot = query.get().await()

                                if (snapshot.exists()) {
                                    // Get the key of the first matching note
                                    val keyToDelete = snapshot.children.first().ref.key

                                    if (keyToDelete != null) {
                                        val noteRef = notesRef.child(keyToDelete)
                                        println("Attempting to delete note with key: $keyToDelete")

                                        // Remove the note from Firebase
                                        noteRef.removeValue().await()
                                        println("Note with ID: ${note.id} successfully deleted.")

                                        // Refresh notes state after deletion
                                        notesState.value = fetchNotes()
                                    } else {
                                        println("No note found with ID: ${note.id}")
                                    }
                                } else {
                                    println("No notes found with ID: ${note.id}")
                                }


                            } catch (e: Exception) {
                                e.printStackTrace()
                                println("Error deleting note with ID: ${e.message}")
                            }
                        }
                    })
                }
            }

            FloatingActionButton(
                onClick = {
                    navController.navigate("addscreen")
                },
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier
                    .size(90.dp, 110.dp)
                    .padding(bottom =  60.dp, end = 30.dp)
                    .align(Alignment.BottomEnd)

            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_input_add),
                    contentDescription = "Add Note",
                    tint = Color.Black
                )
            }
        }
    })

}

@Composable
fun NoteItem_DESIGN(note: dataclass, onDelete: () -> Unit, onEdit: () -> Unit, isLastData: Boolean = false) {

    Surface(
        tonalElevation = 5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 35.dp,
                start = 10.dp,
                end = 10.dp,
                bottom = if (isLastData) 100.dp else 0.dp
            ),
        shape = RectangleShape,
        shadowElevation = 10.dp,
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = note.title,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
            )
            Text(text = note.description)

            Row(
                horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onDelete) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_delete),
                        contentDescription = "Delete Note",
                        tint = Color.Red
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = onEdit
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_edit),
                        contentDescription = "Edit Note",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

        }
    }
}







