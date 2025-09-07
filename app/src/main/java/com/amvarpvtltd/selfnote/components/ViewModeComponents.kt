@file:OptIn(ExperimentalAnimationApi::class)

package com.amvarpvtltd.selfnote.components

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewModule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.amvarpvtltd.selfnote.dataclass
import com.amvarpvtltd.selfnote.design.NoteTheme
import com.amvarpvtltd.selfnote.utils.Constants
import kotlinx.coroutines.*

// Enhanced View Mode enum
enum class ViewMode {
    LIST, GRID, CARD
}

// Enhanced View Mode Manager
object ViewModeManager {
    fun getViewMode(context: android.content.Context): ViewMode {
        val prefs = context.getSharedPreferences(Constants.VIEW_MODE_PREFERENCES, android.content.Context.MODE_PRIVATE)
        val viewModeName = prefs.getString(Constants.VIEW_MODE_KEY, Constants.DEFAULT_VIEW_MODE)
        return when (viewModeName) {
            Constants.VIEW_MODE_LIST -> ViewMode.LIST
            Constants.VIEW_MODE_GRID -> ViewMode.GRID
            else -> ViewMode.CARD
        }
    }

    fun setViewMode(context: android.content.Context, viewMode: ViewMode) {
        val prefs = context.getSharedPreferences(Constants.VIEW_MODE_PREFERENCES, android.content.Context.MODE_PRIVATE)
        val viewModeName = when (viewMode) {
            ViewMode.LIST -> Constants.VIEW_MODE_LIST
            ViewMode.GRID -> Constants.VIEW_MODE_GRID
            ViewMode.CARD -> Constants.VIEW_MODE_CARD
        }
        prefs.edit {
            putString(Constants.VIEW_MODE_KEY, viewModeName)
        }
    }

    fun getViewModeIcon(viewMode: ViewMode): ImageVector {
        return when (viewMode) {
            ViewMode.LIST -> Icons.AutoMirrored.Outlined.ViewList
            ViewMode.GRID -> Icons.Outlined.GridView
            ViewMode.CARD -> Icons.Outlined.ViewModule
        }
    }

    fun getViewModeLabel(viewMode: ViewMode): String {
        return when (viewMode) {
            ViewMode.LIST -> "List View"
            ViewMode.GRID -> "Grid View"
            ViewMode.CARD -> "Card View"
        }
    }
}

@Composable
fun rememberViewModeState(): MutableState<ViewMode> {
    val context = LocalContext.current
    return remember {
        mutableStateOf(ViewModeManager.getViewMode(context))
    }
}

// Enhanced View Mode Toggle Button with animations
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ViewModeToggleButton(
    currentViewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isPressed by remember { mutableStateOf(false) }
    var showTooltip by remember { mutableStateOf(false) }

    // Animation for press effect
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "buttonScale"
    )

    // Color animations
    val animatedContainerColor by animateColorAsState(
        targetValue = if (isPressed) {
            NoteTheme.Secondary.copy(alpha = 0.3f)
        } else {
            NoteTheme.Secondary.copy(alpha = 0.1f)
        },
        animationSpec = tween(150),
        label = "containerColor"
    )

    Box {
        Card(
            modifier = modifier
                .size(48.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(
                    elevation = if (isPressed) 8.dp else 4.dp,
                    shape = CircleShape,
                    ambientColor = NoteTheme.Secondary.copy(alpha = 0.1f),
                    spotColor = NoteTheme.Secondary.copy(alpha = 0.2f)
                )
                .clickable {
                    isPressed = true
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

                    val nextMode = when (currentViewMode) {
                        ViewMode.CARD -> ViewMode.LIST
                        ViewMode.LIST -> ViewMode.GRID
                        ViewMode.GRID -> ViewMode.CARD
                    }

                    val toastMessage = "📱 ${ViewModeManager.getViewModeLabel(nextMode)} activated"
                    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                    onViewModeChange(nextMode)

                    // Reset pressed state using structured concurrency
                    scope.launch {
                        delay(100)
                        isPressed = false
                    }
                },
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = animatedContainerColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NoteTheme.Secondary.copy(alpha = 0.1f),
                                NoteTheme.Secondary.copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentViewMode,
                    transitionSpec = {
                        (scaleIn(
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        ) + fadeIn(animationSpec = tween(300))).togetherWith(
                            scaleOut(
                                animationSpec = tween(150, easing = FastOutLinearInEasing)
                            ) + fadeOut(animationSpec = tween(150))
                        )
                    },
                    label = "viewModeIcon"
                ) { viewMode ->
                    Icon(
                        imageVector = ViewModeManager.getViewModeIcon(viewMode),
                        contentDescription = ViewModeManager.getViewModeLabel(viewMode),
                        tint = NoteTheme.Secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Tooltip (optional)
        if (showTooltip) {
            Card(
                modifier = Modifier
                    .offset(y = (-60).dp)
                    .padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = ViewModeManager.getViewModeLabel(currentViewMode),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.inverseOnSurface,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// Notes Display with smooth transitions
@Composable
fun NotesDisplay(
    notes: List<dataclass>,
    viewMode: ViewMode,
    onView: (dataclass) -> Unit,
    onEdit: (dataclass) -> Unit,
    onDelete: (dataclass) -> Unit,
    onShare: (dataclass) -> Unit,
    onReminder: (dataclass) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = viewMode,
        transitionSpec = {
            (slideInHorizontally(
                initialOffsetX = { if (targetState.ordinal > initialState.ordinal) 300 else -300 },
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(400))).togetherWith(
                slideOutHorizontally(
                    targetOffsetX = { if (targetState.ordinal > initialState.ordinal) -300 else 300 },
                    animationSpec = tween(300, easing = FastOutLinearInEasing)
                ) + fadeOut(animationSpec = tween(300))
            )
        },
        label = "notesDisplay"
    ) { currentViewMode ->
        when (currentViewMode) {
            ViewMode.CARD -> NotesCardView(notes, onView, onEdit, onDelete, onShare, onReminder, modifier)
            ViewMode.LIST -> NotesListView(notes, onView, onEdit, onDelete, onShare, onReminder, modifier)
            ViewMode.GRID -> NotesGridView(notes, onView, onEdit, onDelete, onShare, onReminder, modifier)
        }
    }
}

// List View implementation
@Composable
private fun NotesListView(
    notes: List<dataclass>,
    onView: (dataclass) -> Unit,
    onEdit: (dataclass) -> Unit,
    onDelete: (dataclass) -> Unit,
    onShare: (dataclass) -> Unit,
    onReminder: (dataclass) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 8.dp,
            end = 16.dp,
            bottom = 80.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp) // Tighter spacing for list view
    ) {
        itemsIndexed(
            items = notes,
            key = { _, note -> note.id } // Add stable keys for better performance
        ) { index, note ->
            AnimatedVisibility(
                visible = true,
                enter = slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(
                        durationMillis = 300,
                        delayMillis = index * 50,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(300)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                NoteCard(
                    note = note,
                    onClick = { onView(note) },
                    onEdit = { onEdit(note) },
                    onDelete = { onDelete(note) },
                    onShare = { onShare(note) },
                    onReminder = { onReminder(note) }
                )
            }
        }
    }
}

// Grid View implementation
@Composable
private fun NotesGridView(
    notes: List<dataclass>,
    onView: (dataclass) -> Unit,
    onEdit: (dataclass) -> Unit,
    onDelete: (dataclass) -> Unit,
    onShare: (dataclass) -> Unit,
    onReminder: (dataclass) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val gridCells = when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> GridCells.Adaptive(minSize = 220.dp)
        else -> GridCells.Fixed(2)
    }

    LazyVerticalGrid(
        columns = gridCells,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 12.dp,
            top = 12.dp,
            end = 12.dp,
            bottom = 80.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = notes,
            key = { _, note -> note.id }
        ) { index, note ->
            AnimatedVisibility(
                visible = true,
                enter = scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(
                        durationMillis = 300,
                        delayMillis = index * 50,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(300)
                )
            ) {
                NoteCard(
                    note = note,
                    onClick = { onView(note) },
                    onEdit = { onEdit(note) },
                    onDelete = { onDelete(note) },
                    onShare = { onShare(note) },
                    onReminder = { onReminder(note) }
                )
            }
        }
    }
}

// Card View implementation (similar animations and spacing)
@Composable
private fun NotesCardView(
    notes: List<dataclass>,
    onView: (dataclass) -> Unit,
    onEdit: (dataclass) -> Unit,
    onDelete: (dataclass) -> Unit,
    onShare: (dataclass) -> Unit,
    onReminder: (dataclass) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 8.dp,
            end = 16.dp,
            bottom = 80.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(
            items = notes,
            key = { _, note -> note.id }
        ) { index, note ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(
                        durationMillis = 300,
                        delayMillis = index * 50,
                        easing = FastOutSlowInEasing
                    )
                ) + fadeIn(
                    animationSpec = tween(300)
                )
            ) {
                NoteCard(
                    note = note,
                    onClick = { onView(note) },
                    onEdit = { onEdit(note) },
                    onDelete = { onDelete(note) },
                    onShare = { onShare(note) },
                    onReminder = { onReminder(note) }
                )
            }
        }
    }
}

// Note Card Colors and Utilities
val noteCardColors = listOf(
    Color(0xFFF8FAFC) to Color(0xFF0EA5E9), // Blue
    Color(0xFFF0FDF4) to Color(0xFF10B981), // Green
    Color(0xFFFEF3C7) to Color(0xFFF59E0B), // Amber
    Color(0xFFFDF2F8) to Color(0xFFEC4899), // Pink
    Color(0xFFF3E8FF) to Color(0xFF8B5CF6), // Purple
    Color(0xFFECFDF5) to Color(0xFF059669), // Emerald
    Color(0xFFFFF1F2) to Color(0xFFE11D48), // Rose
    Color(0xFFF0F4FF) to Color(0xFF6366F1)  // Indigo
)
