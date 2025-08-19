package com.amvarpvtltd.selfnote.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.amvarpvtltd.selfnote.repository.NoteRepository
import com.amvarpvtltd.selfnote.components.*
import com.amvarpvtltd.selfnote.utils.Constants
import dataclass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewNoteScreen(navController: NavHostController, noteId: String?) {
    var note by remember { mutableStateOf<dataclass?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val noteRepository = remember { NoteRepository() }
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    // Load note data
    LaunchedEffect(noteId) {
        if (noteId != null) {
            try {
                val result = noteRepository.loadNote(noteId)
                if (result.isSuccess) {
                    note = result.getOrNull()
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, Constants.ERROR_LOADING_MESSAGE, Toast.LENGTH_LONG).show()
                        navController.navigateUp()
                    }
                }
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
            navController.navigateUp()
        }
    }

    // Delete note function
    fun deleteNote() {
        if (noteId == null) return
        scope.launch(Dispatchers.IO) {
            try {
                val result = noteRepository.deleteNote(noteId, context)
                if (result.isSuccess) {
                    withContext(Dispatchers.Main) {
                        navController.navigate("noteScreen")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, Constants.ERROR_DELETING_MESSAGE, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    DeleteConfirmationDialog(
        showDialog = showDeleteDialog,
        onDismiss = { showDeleteDialog = false },
        onConfirm = {
            showDeleteDialog = false
            deleteNote()
        },
        title = note?.title ?: "",
        message = "Are you sure you want to delete this note?"
    )

    NoteScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                NoteTopAppBar(
                    title = "View Note",
                    isLoading = isLoading,
                    navController = navController,
                    showDeleteAction = note != null,
                    onDeleteClick = { showDeleteDialog = true }
                )
            },
            floatingActionButton = {
                if (note != null) {
                    ActionButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate("addscreen/${note?.id}")
                        },
                        text = "Edit Note",
                        icon = Icons.Outlined.Edit,
                        modifier = Modifier
                    )
                }
            }
        ) { paddingValues ->
            if (isLoading) {
                LoadingCard("Loading note...", "Please wait a moment")
            } else if (note == null) {
                // Error state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    EmptyStateCard(
                        icon = Icons.Outlined.ErrorOutline,
                        title = "Note Not Found",
                        description = "The note you're looking for doesn't exist or has been deleted.",
                        buttonText = "Go Back",
                        onButtonClick = { navController.navigateUp() }
                    )
                }
            } else {
                // Display note content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(Constants.PADDING_MEDIUM.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(Constants.PADDING_LARGE.dp)
                ) {
                    // Title Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = NoteTheme.Surface),
                        shape = RoundedCornerShape(Constants.CORNER_RADIUS_LARGE.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(Constants.PADDING_LARGE.dp)
                        ) {
                            Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Title,
                                    contentDescription = null,
                                    tint = NoteTheme.Primary,
                                    modifier = Modifier.size(Constants.ICON_SIZE_MEDIUM.dp)
                                )
                                Spacer(modifier = Modifier.width(Constants.PADDING_SMALL.dp))
                                Text(
                                    text = "Title",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = NoteTheme.OnSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(Constants.PADDING_MEDIUM.dp))

                            Text(
                                text = note!!.title,
                                style = MaterialTheme.typography.headlineSmall,
                                color = NoteTheme.OnSurface,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 32.sp
                            )
                        }
                    }

                    // Description Card (only show if description exists)
                    if (note!!.description.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = NoteTheme.Surface),
                            shape = RoundedCornerShape(Constants.CORNER_RADIUS_LARGE.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(Constants.PADDING_LARGE.dp)
                            ) {
                                Row(
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Description,
                                        contentDescription = null,
                                        tint = NoteTheme.Secondary,
                                        modifier = Modifier.size(Constants.ICON_SIZE_MEDIUM.dp)
                                    )
                                    Spacer(modifier = Modifier.width(Constants.PADDING_SMALL.dp))
                                    Text(
                                        text = "Description",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = NoteTheme.OnSurface,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(Constants.PADDING_MEDIUM.dp))

                                Text(
                                    text = note!!.description,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = NoteTheme.OnSurfaceVariant,
                                    lineHeight = 28.sp,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    } else {
                        // No description card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = NoteTheme.SurfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(Constants.CORNER_RADIUS_LARGE.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(Constants.PADDING_LARGE.dp),
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.TextSnippet,
                                    contentDescription = null,
                                    tint = NoteTheme.OnSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(Constants.PADDING_MEDIUM.dp))
                                Text(
                                    text = "No description added",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = NoteTheme.OnSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }

                    // Add some bottom padding for FAB
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
