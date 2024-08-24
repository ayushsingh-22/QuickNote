package com.amvarpvtltd.selfnote.design

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.withContext
import myGlobalMobileDeviceId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(navController: NavHostController) {
    val notesState = remember { mutableStateOf<List<dataclass>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            notesState.value = fetchNotes()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(top = 30.dp),
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
        },
        content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    notesState.value.forEachIndexed { index, note ->
                        val isLastData = index == notesState.value.lastIndex
                        NoteItem_DESIGN(
                            note = note,
                            index = index,
                            isLastData = isLastData,
                            onEdit = {
                                navController.navigate("addscreen/${note.id}")
                            },
                            onDelete = {
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        val database = FirebaseDatabase.getInstance()
                                        val notesRef = database.getReference("notes").child(myGlobalMobileDeviceId)

                                        val query = notesRef.orderByChild("id").equalTo(note.id)
                                        val snapshot = query.get().await()

                                        if (snapshot.exists()) {
                                            val keyToDelete = snapshot.children.first().ref.key

                                            if (keyToDelete != null) {
                                                val noteRef = notesRef.child(keyToDelete)
                                                noteRef.removeValue().await()

                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(navController.context, "Note deleted successfully", Toast.LENGTH_SHORT).show()
                                                }

                                                notesState.value = fetchNotes()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }

                        )
                    }
                }

                FloatingActionButton(
                    onClick = {
                        navController.navigate("addscreen")
                    },
                    shape = RoundedCornerShape(5.dp),
                    modifier = Modifier
                        .size(90.dp, 110.dp)
                        .padding(bottom = 60.dp, end = 30.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_input_add),
                        contentDescription = "Add Note",
                        tint = Color.Black
                    )
                }
            }
        }
    )
}

val cardColors = listOf(
    Color(0xFFFFCDD2),
    Color(0xFFC8E6C9),
    Color(0xFFBBDEFB),
    Color(0xFFFFF9C4),
    Color(0xFFD1C4E9),
    Color(0xFFFFE0B2),
    Color(0xFFE0E0E0),
)

@Composable
fun NoteItem_DESIGN(
    note: dataclass,
    index: Int,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    isLastData: Boolean = false
) {

    val cardColor = cardColors[index % cardColors.size]

    Surface(onClick = {},
        color = cardColor,
        tonalElevation = 5.dp,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Unspecified)
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
                color = Color.Black
            )
            Text(
                text = note.description,
                color = Color.Black
            )

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

                IconButton(onClick = onEdit) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_edit),
                        contentDescription = "Edit Note",
                        tint = Color.DarkGray
                    )
                }
            }
        }
    }
}
