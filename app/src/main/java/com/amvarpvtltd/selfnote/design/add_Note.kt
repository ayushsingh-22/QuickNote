package com.amvarpvtltd.selfnote.design

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.database.FirebaseDatabase
import dataclass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import myGlobalMobileDeviceId


@Composable
fun AddScreen(navController: NavHostController, noteId: String?) {

//    val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels
//    val screenHeight = LocalContext.current.resources.displayMetrics.heightPixels


    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance()
    val notesRef = database.getReference("notes").child(myGlobalMobileDeviceId)
    val scope = rememberCoroutineScope()


    LaunchedEffect(noteId) {
        if (noteId != null) {
            try {

                val query = notesRef.orderByChild("id").equalTo(noteId)
                val snapshot = query.get().await()

                if (snapshot.exists()) {
                    val keyToUpdate = snapshot.children.first().key
                    if (keyToUpdate != null) {
                        val noteSnapshot = notesRef.child(keyToUpdate).get().await()
                        val note = noteSnapshot.getValue(dataclass::class.java)
                        note?.let {
                            title = it.title
                            description = it.description
                        }
                    } else {
                        Log.w("Firebase", "No key found for the note ID: $noteId")
                    }
                } else {
                    Log.w("Firebase", "No note found with ID: $noteId")
                }
            } catch (e: Exception) {
                Log.e("Firebase", "Error fetching note", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error fetching note: ${e.message}", Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    fun saveNote() {

        if (title.length < 5 && description.length < 5) {
            Toast.makeText(context, "Title and description must be at least 5 characters long.", Toast.LENGTH_LONG).show()
            return
        }

        val note = dataclass(title = title, description = description)
        note.mymobiledeviceid = myGlobalMobileDeviceId

        scope.launch(Dispatchers.IO) {
            try {
                val deviceIdRef = database.getReference("notes")
                    .child(myGlobalMobileDeviceId) // Create subdirectory

                if (noteId == null) {
                    deviceIdRef.push()
                        .setValue(note)
                        .await() // Wait for the operation to complete
                    Log.d("Firebase", "Note saved successfully")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Note saved!", Toast.LENGTH_LONG).show()
                    }
                } else {
                    val query = notesRef.orderByChild("id").equalTo(noteId)
                    val keyToUpdate = query.get().await().children.firstOrNull()?.key
                    note.id = noteId

                    deviceIdRef.child(keyToUpdate!!)
                        .setValue(note)
                        .await() // Wait for the operation to complete
                    Log.d("Firebase", "Note updated successfully")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Note updated!", Toast.LENGTH_LONG).show()
                    }
                }

                withContext(Dispatchers.Main) {
                    navController.navigate("noteScreen")
                }
            } catch (e: Exception) {
                Log.e("Firebase", "Error saving/updating note", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Error saving/updating note: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .size(50.dp)
                .background(color = MaterialTheme.colorScheme.primary),
            shape = RectangleShape
        ) {}

        TextField(
            value = title,
            onValueChange = { title = it },
            singleLine = true,
            maxLines = 2,
            shape = RectangleShape,
            modifier = Modifier
                .fillMaxWidth(),
            placeholder = {
                if (title.isEmpty()) {
                    Text(
                        text = "Title",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            },
            textStyle = LocalTextStyle.current.copy(
                fontWeight = if (title.isEmpty()) FontWeight.Normal else FontWeight.ExtraBold,
                fontSize = 25.sp
            )
        )

        Surface {

            TextField(
                value = description,
                onValueChange = { description = it },
                singleLine = false,
                modifier = Modifier
                    .fillMaxSize()
                    .height(150.dp),
                placeholder = {
                    if (description.isEmpty()) {
                        Text(
                            text = "Description",
                            fontSize = 25.sp
                        )
                    }
                },
                textStyle = LocalTextStyle.current.copy(
                    fontWeight = if (description.isEmpty()) {FontWeight.Normal}
                    else {FontWeight.SemiBold},
                    fontSize = 20.sp
                )
            )

            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                FloatingActionButton(
                    onClick = { saveNote() },
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier
                        .padding(
                            bottom = 60.dp,
                            end = 20.dp
                        )
                        .size(50.dp)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_save),
                        contentDescription = "Add Note",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

