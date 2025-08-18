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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.amvarpvtltd.selfnote.ui.theme.Lobster_Font
import com.google.firebase.database.FirebaseDatabase
import dataclass
import fetchNotes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import myGlobalMobileDeviceId
import androidx.compose.ui.unit.IntOffset

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

    // Refresh functionality
    fun refreshNotes() {
        scope.launch(Dispatchers.IO) {
            isRefreshingState.value = true
            try {
                delay(500) // Small delay for better UX
                notesState.value = fetchNotes()
            } finally {
                isRefreshingState.value = false
                if (isLoadingState.value) isLoadingState.value = false
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshNotes()
    }

    // Animated background gradient
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient"
    )

    val backgroundBrush = Brush.verticalGradient(colors = listOf(NoteTheme.Background, NoteTheme.SurfaceVariant.copy(alpha = 0.3f + animatedOffset * 0.1f), NoteTheme.Background))
    Scaffold(
        modifier = Modifier.background(backgroundBrush),
        containerColor = Color.Transparent,
        topBar = {
            EnhancedTopAppBar(
                notesCount = notesState.value.size,
                onRefresh = { refreshNotes() },
                isLoading = isRefreshingState.value
            )

        },
        floatingActionButton = {
            EnhancedFloatingActionButton(
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
                .background(backgroundBrush)
                .padding(paddingValues)
        ) {
            when {
                isLoadingState.value -> {
                    LoadingState()
                }

                notesState.value.isEmpty() -> {
                    EmptyState(
                        onCreateNote = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigate("addscreen")
                        }
                    )
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 8.dp,
                            end = 16.dp,
                            bottom = 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(
                            items = notesState.value,
                            key = { _, note -> note.id }
                        ) { index, note ->
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec = tween(durationMillis = 600, delayMillis = index * 100)
                                ) + scaleIn(
                                    initialScale = 0.9f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                ) + fadeIn(animationSpec = tween(durationMillis = 600, delayMillis = index * 100)),
                                modifier = Modifier.animateItemPlacement(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy
                                    )
                                )
                            ) {
                                EnhancedNoteItem(
                                    note = note,
                                    index = index,
                                    onEdit = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        navController.navigate("addscreen/${note.id}")
                                    },
                                    onDelete = {
                                        scope.launch(Dispatchers.IO) {
                                            try {
                                                val database = FirebaseDatabase.getInstance()
                                                val notesRef = database.getReference("notes")
                                                    .child(myGlobalMobileDeviceId)

                                                val query = notesRef.orderByChild("id").equalTo(note.id)
                                                val snapshot = query.get().await()

                                                if (snapshot.exists()) {
                                                    for (childSnapshot in snapshot.children) {
                                                        childSnapshot.ref.removeValue().await()
                                                    }
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show()
                                                        refreshNotes()
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(context, "Error deleting note", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    }
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
fun EnhancedTopAppBar(
    notesCount: Int,
    onRefresh: () -> Unit,
    isLoading: Boolean
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isLoading) 360f else 0f,
        animationSpec = if (isLoading) {
            infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        } else {
            spring()
        },
        label = "refresh_rotation"
    )

    val scaleAnimation by animateFloatAsState(
        targetValue = if (isLoading) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "refresh_scale"
    )

    Box(modifier = Modifier.padding(15.dp)) {
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Notes",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        fontFamily = Lobster_Font,
                        color = NoteTheme.Primary
                    )

                    AnimatedVisibility(
                        visible = isLoading,
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        Spacer(modifier = Modifier.width(12.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = NoteTheme.Secondary
                        )
                    }
                }
            },
            actions = {
                Card(
                    modifier = Modifier
                        .scale(scaleAnimation)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onRefresh() },
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLoading) NoteTheme.Secondary.copy(alpha = 0.1f)
                        else NoteTheme.Primary.copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isLoading) 4.dp else 2.dp
                    )
                ) {
                    Box(
                        modifier = Modifier.padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer(rotationZ = rotationAngle),
                            tint = if (isLoading) NoteTheme.Secondary else NoteTheme.Primary
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )
    }
}

@Composable
fun EnhancedFloatingActionButton(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "fab_scale"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 12.dp,
        animationSpec = spring(),
        label = "fab_elevation"
    )

    FloatingActionButton(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = Modifier
            .size(64.dp)
            .scale(scale)
            .shadow(shadowElevation, CircleShape),
        shape = CircleShape,
        containerColor = NoteTheme.Primary,
        contentColor = NoteTheme.OnPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Note",
            modifier = Modifier.size(28.dp)
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(32.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NoteTheme.Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = NoteTheme.Primary,
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Loading your notes...",
                    style = MaterialTheme.typography.titleMedium,
                    color = NoteTheme.OnSurface,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Please wait a moment",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NoteTheme.OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyState(onCreateNote: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = NoteTheme.Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                // Animated empty state icon
                val infiniteTransition = rememberInfiniteTransition(label = "empty_animation")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.9f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "empty_scale"
                )

                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    NoteTheme.Primary.copy(alpha = 0.1f),
                                    NoteTheme.Primary.copy(alpha = 0.05f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.NoteAdd,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = NoteTheme.Primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "No notes yet",
                    style = MaterialTheme.typography.headlineSmall,
                    color = NoteTheme.OnSurface,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Create your first note to capture\nyour thoughts and ideas",
                    style = MaterialTheme.typography.bodyLarge,
                    color = NoteTheme.OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onCreateNote,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NoteTheme.Primary,
                        contentColor = NoteTheme.OnPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Create Your First Note",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedNoteItem(
    note: dataclass,
    index: Int,
    onDelete: () -> Unit,
    onEdit: () -> Unit
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
                onEdit()
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

            Column(
                modifier = Modifier.padding(24.dp)
            ) {
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
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 24.sp
                        ),
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
                    // Delete button
                    Card(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                showDeleteDialog = true
                            },
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = NoteTheme.Error.copy(alpha = 0.1f)
                        )
                    ) {
                        Box(
                            modifier = Modifier.padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(20.dp),
                                tint = NoteTheme.Error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Edit button
                    Card(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onEdit()
                            },
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = accentColor.copy(alpha = 0.1f)
                        )
                    ) {
                        Box(
                            modifier = Modifier.padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(20.dp),
                                tint = accentColor
                            )
                        }
                    }
                }
            }
        }
    }

    // Enhanced delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            NoteTheme.Error.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        tint = NoteTheme.Error,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    "Delete Note?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = NoteTheme.OnSurface
                )
            },
            text = {
                Column {
                    Text(
                        "Are you sure you want to delete:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NoteTheme.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "\"${note.title}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NoteTheme.OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This action cannot be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NoteTheme.Error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NoteTheme.Error,
                        contentColor = NoteTheme.OnPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = NoteTheme.OnSurfaceVariant
                    )
                ) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = NoteTheme.Surface
        )
    }

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