package com.amvarpvtltd.selfnote.design

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.TextSnippet
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.amvarpvtltd.selfnote.components.ActionButton
import com.amvarpvtltd.selfnote.components.DeleteConfirmationDialog
import com.amvarpvtltd.selfnote.components.EmptyStateCard
import com.amvarpvtltd.selfnote.components.IconActionButton
import com.amvarpvtltd.selfnote.components.LoadingCard
import com.amvarpvtltd.selfnote.components.NoteScreenBackground
import com.amvarpvtltd.selfnote.components.NoteTopAppBar
import com.amvarpvtltd.selfnote.components.OfflineActionDialog
import com.amvarpvtltd.selfnote.components.OfflineIndicator
import com.amvarpvtltd.selfnote.components.rememberOfflineHandler
import com.amvarpvtltd.selfnote.repository.NoteRepository
import com.amvarpvtltd.selfnote.utils.Constants
import com.amvarpvtltd.selfnote.utils.NetworkManager
import dataclass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewNoteScreen(navController: NavHostController, noteId: String?) {
    var note by remember { mutableStateOf<dataclass?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showOfflineDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val noteRepository = remember { NoteRepository(context) }
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    // Network management
    val networkManager = remember { NetworkManager.getInstance(context) }
    val isOnline by networkManager.isOnline.collectAsState()
    val offlineHandler = rememberOfflineHandler(
        isOnline = isOnline,
        onOfflineAction = { showOfflineDialog = true }
    )

    // Load note data
    LaunchedEffect(noteId) {
        if (noteId != null) {
            try {
                val result = noteRepository.loadNote(noteId, context)
                if (result.isSuccess) {
                    note = result.getOrNull()
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error loading note: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG)
                            .show()
                        navController.navigateUp()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error loading note: ${e.message}", Toast.LENGTH_LONG).show()
                    navController.navigateUp()
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
                        if (!isOnline) {
                            Toast.makeText(context, Constants.OFFLINE_SAVE_MESSAGE, Toast.LENGTH_SHORT).show()
                            navController.navigate("offlineScreen") {
                                popUpTo("viewnote/${noteId}") { inclusive = true }
                            }
                        } else {
                            navController.navigate("noteScreen")
                        }
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

    // Add offline dialog
    OfflineActionDialog(
        showDialog = showOfflineDialog,
        onDismiss = { showOfflineDialog = false },
        title = "Offline Mode",
        message = "You can still view and edit this note offline. Changes will sync when you're back online.",
        actionText = "Edit Offline",
        onActionClick = {
            navController.navigate("addscreen/${note?.id}")
        }
    )

    NoteScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))

                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "View Note",
                                    fontWeight = FontWeight.Bold,
                                    color = NoteTheme.OnSurface,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                if (isLoading) {
                                    Spacer(modifier = Modifier.width(Constants.PADDING_SMALL.dp))
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(Constants.ICON_SIZE_MEDIUM.dp),
                                        strokeWidth = 2.dp,
                                        color = NoteTheme.Primary
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            IconActionButton(
                                onClick = {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    navController.navigateUp()
                                },
                                icon = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                containerColor = NoteTheme.Primary.copy(alpha = 0.1f),
                                contentColor = NoteTheme.Primary,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        },
                        actions = {
                            Row(
                                modifier = Modifier.padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Add offline indicator to actions
                                OfflineIndicator(
                                    isOnline = isOnline,
                                    showText = false
                                )

                                // Delete action
                                if (note != null) {
                                    IconActionButton(
                                        onClick = {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            offlineHandler.executeIfOnline {
                                                showDeleteDialog = true
                                            }
                                        },
                                        icon = Icons.Outlined.Delete,
                                        contentDescription = "Delete",
                                        containerColor = NoteTheme.Error.copy(alpha = 0.1f),
                                        contentColor = NoteTheme.Error
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            },
            floatingActionButton = {
                if (note != null) {
                    ActionButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            // Allow editing offline - notes are cached locally
                            navController.navigate("addscreen/${note?.id}")
                        },
                        text = if (isOnline) "Edit Note" else "Edit Offline",
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
