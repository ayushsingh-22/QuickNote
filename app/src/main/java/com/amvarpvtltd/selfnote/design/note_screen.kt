package com.amvarpvtltd.selfnote.design

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.amvarpvtltd.selfnote.repository.NoteRepository
import com.amvarpvtltd.selfnote.utils.Constants
import com.amvarpvtltd.selfnote.components.*
import dataclass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotesScreen(navController: NavHostController) {
    val notesState = remember { mutableStateOf<List<dataclass>>(emptyList()) }
    val isLoadingState = remember { mutableStateOf(true) }
    val isRefreshingState = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val noteRepository = remember { NoteRepository() }

    // Refresh functionality
    fun refreshNotes() {
        scope.launch(Dispatchers.IO) {
            isRefreshingState.value = true
            try {
                delay(Constants.REFRESH_DELAY)
                val result = noteRepository.fetchNotes()
                if (result.isSuccess) {
                    notesState.value = result.getOrNull() ?: emptyList()
                }
            } finally {
                isRefreshingState.value = false
                if (isLoadingState.value) isLoadingState.value = false
            }
        }
    }

    // Delete note function
    fun deleteNote(noteId: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val result = noteRepository.deleteNote(noteId, context)
                if (result.isSuccess) {
                    withContext(Dispatchers.Main) { refreshNotes() }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, Constants.ERROR_DELETING_MESSAGE, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) { refreshNotes() }

    NoteScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                NoteTopAppBar(
                    title = "My Notes",
                    isLoading = isRefreshingState.value,
                    onRefresh = { refreshNotes() },

                )
            },
            floatingActionButton = {
                AnimatedFloatingActionButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        navController.navigate("addscreen")
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    isLoadingState.value -> {
                        LoadingCard("Loading your notes...", "Please wait a moment")
                    }

                    notesState.value.isEmpty() -> {
                        EmptyStateCard(
                            icon = Icons.AutoMirrored.Outlined.NoteAdd,
                            title = "No notes yet",
                            description = "Create your first note to capture\nyour thoughts and ideas",
                            buttonText = "Create Your First Note",
                            onButtonClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                navController.navigate("addscreen")
                            }
                        )
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(
                                start = Constants.PADDING_MEDIUM.dp,
                                top = Constants.PADDING_SMALL.dp,
                                end = Constants.PADDING_MEDIUM.dp,
                                bottom = 100.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(Constants.PADDING_MEDIUM.dp)
                        ) {
                            itemsIndexed(
                                items = notesState.value,
                                key = { _, note -> note.id }
                            ) { index, note ->
                                NoteCard(
                                    note = note,
                                    index = index,
                                    onView = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        navController.navigate("viewnote/${note.id}")
                                    },
                                    onEdit = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        navController.navigate("addscreen/${note.id}")
                                    },
                                    onDelete = { deleteNote(note.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteCard(
    note: dataclass,
    index: Int,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onView: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    val cardColorPair = noteCardColors[index % noteCardColors.size]
    val backgroundColor = cardColorPair.first
    val accentColor = cardColorPair.second

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 600, delayMillis = index * 100)
        ) + scaleIn(
            initialScale = 0.9f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = index * 100))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isPressed = true
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onView() // Changed from onEdit to onView
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isPressed) 8.dp else 4.dp
            )
        ) {
            Box {
                // Accent gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.8f),
                                    accentColor.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(modifier = Modifier.padding(24.dp)) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            lineHeight = 28.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = NoteTheme.OnSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (note.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = note.description,
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                            color = NoteTheme.OnSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconActionButton(
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                showDeleteDialog = true
                            },
                            icon = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            containerColor = NoteTheme.Error.copy(alpha = 0.1f),
                            contentColor = NoteTheme.Error
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        IconActionButton(
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onEdit()
                            },
                            icon = Icons.Outlined.Edit,
                            contentDescription = "Edit",
                            containerColor = accentColor.copy(alpha = 0.1f),
                            contentColor = accentColor
                        )
                    }
                }
            }
        }
    }

    DeleteConfirmationDialog(
        showDialog = showDeleteDialog,
        onDismiss = { showDeleteDialog = false },
        onConfirm = {
            showDeleteDialog = false
            onDelete()
        },
        title = note.title,
        message = "Are you sure you want to delete:"
    )

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

// Define note card colors
val noteCardColors = listOf(
    Color(0xFFF0F9FF) to Color(0xFF0EA5E9), // Blue
    Color(0xFFF0FDF4) to Color(0xFF10B981), // Green
    Color(0xFFFEF3C7) to Color(0xFFF59E0B), // Amber
    Color(0xFFFDF2F8) to Color(0xFFEC4899), // Pink
    Color(0xFFF3E8FF) to Color(0xFF8B5CF6), // Purple
    Color(0xFFECFDF5) to Color(0xFF059669), // Emerald
    Color(0xFFFFF1F2) to Color(0xFFE11D48), // Rose
    Color(0xFFF0F4FF) to Color(0xFF6366F1)  // Indigo
)
