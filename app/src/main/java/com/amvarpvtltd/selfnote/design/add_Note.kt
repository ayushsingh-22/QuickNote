package com.amvarpvtltd.selfnote.design

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material3.ButtonDefaults
import com.amvarpvtltd.selfnote.repository.NoteRepository
import com.amvarpvtltd.selfnote.utils.ValidationUtils
import com.amvarpvtltd.selfnote.utils.UIUtils
import com.amvarpvtltd.selfnote.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(navController: NavHostController, noteId: String?) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showBackDialog by remember { mutableStateOf(false) }
    var titleFocused by remember { mutableStateOf(false) }
    var descriptionFocused by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val noteRepository = remember { NoteRepository() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val titleFocusRequester = remember { FocusRequester() }
    val hapticFeedback = LocalHapticFeedback.current

    val isEditing = noteId != null
    val canSave = ValidationUtils.canSaveNote(title, description)
    val hasContent = title.trim().isNotEmpty() || description.trim().isNotEmpty()

    val titleProgress = UIUtils.calculateProgress(title.length, Constants.TITLE_MAX_LENGTH)
    val descriptionProgress = UIUtils.calculateProgress(description.length, Constants.DESCRIPTION_MAX_LENGTH)

    val titleCountColor by animateColorAsState(
        targetValue = UIUtils.getProgressColor(title.length),
        animationSpec = UIUtils.getColorAnimationSpec(),
        label = "title_count_color"
    )

    val descCountColor by animateColorAsState(
        targetValue = UIUtils.getProgressColor(description.length),
        animationSpec = UIUtils.getColorAnimationSpec(),
        label = "desc_count_color"
    )

    // Load existing note data
    LaunchedEffect(noteId) {
        if (noteId != null) {
            isLoading = true
            try {
                val result = noteRepository.loadNote(noteId)
                if (result.isSuccess) {
                    val note = result.getOrNull()
                    note?.let {
                        title = it.title
                        description = it.description
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, Constants.ERROR_LOADING_MESSAGE, Toast.LENGTH_LONG).show()
                    }
                }
            } finally {
                isLoading = false
            }
        }
    }

    // Save note function
    fun saveNote() {
        if (!canSave) {
            Toast.makeText(context, Constants.VALIDATION_WARNING_MESSAGE, Toast.LENGTH_LONG).show()
            return
        }

        isSaving = true
        scope.launch(Dispatchers.IO) {
            try {
                val result = noteRepository.saveNote(title, description, noteId, context)
                if (result.isSuccess) {
                    withContext(Dispatchers.Main) {
                        navController.navigate("noteScreen")
                    }
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isSaving = false
                }
            }
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
                Log.e("AddScreen", "Error in deleteNote", e)
            }
        }
    }

    // Background gradient
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            NoteTheme.Background,
            NoteTheme.SurfaceVariant.copy(alpha = 0.3f),
            NoteTheme.Background
        )
    )

    // Enhanced delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    NoteTheme.Error.copy(alpha = 0.2f),
                                    NoteTheme.Error.copy(alpha = 0.05f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        tint = NoteTheme.Error,
                        modifier = Modifier.size(32.dp)
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
                        "Are you sure you want to delete this note?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = NoteTheme.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = NoteTheme.ErrorContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(Constants.CORNER_RADIUS_SMALL.dp)
                    ) {
                        Text(
                            text = "\"$title\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NoteTheme.OnSurface,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(Constants.CORNER_RADIUS_SMALL.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "This action cannot be undone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = NoteTheme.Error,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDeleteDialog = false
                        deleteNote()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NoteTheme.Error,
                        contentColor = NoteTheme.OnPrimary
                    ),
                    shape = RoundedCornerShape(Constants.CORNER_RADIUS_SMALL.dp)
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NoteTheme.OnSurfaceVariant
                    ),
                    shape = RoundedCornerShape(Constants.CORNER_RADIUS_SMALL.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(Constants.CORNER_RADIUS_XL.dp),
            containerColor = NoteTheme.Surface
        )
    }

    // Back confirmation dialog
    if (showBackDialog) {
        AlertDialog(
            onDismissRequest = { showBackDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    NoteTheme.Warning.copy(alpha = 0.2f),
                                    NoteTheme.Warning.copy(alpha = 0.05f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = NoteTheme.Warning,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    "Discard Changes?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = NoteTheme.OnSurface
                )
            },
            text = {
                Text(
                    "You have unsaved changes. Are you sure you want to go back? Your changes will be lost.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = NoteTheme.OnSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        showBackDialog = false
                        navController.navigateUp()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NoteTheme.Warning,
                        contentColor = NoteTheme.OnPrimary
                    ),
                    shape = RoundedCornerShape(Constants.CORNER_RADIUS_SMALL.dp)
                ) {
                    Icon(
                        Icons.Outlined.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Discard", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showBackDialog = false },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NoteTheme.OnSurfaceVariant
                    ),
                    shape = RoundedCornerShape(Constants.CORNER_RADIUS_SMALL.dp)
                ) {
                    Text("Keep Editing", fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(Constants.CORNER_RADIUS_XL.dp),
            containerColor = NoteTheme.Surface
        )
    }

    Scaffold(
        modifier = Modifier.background(Brush.verticalGradient(
            colors = listOf(
                NoteTheme.Background,
                NoteTheme.SurfaceVariant.copy(alpha = 0.3f),
                NoteTheme.Background
            )
        )),
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEditing) "Edit Note" else "New Note",
                            fontWeight = FontWeight.Bold,
                            color = NoteTheme.OnSurface,
                            style = MaterialTheme.typography.titleLarge
                        )

                        if (isLoading) {
                            Spacer(modifier = Modifier.width(Constants.CORNER_RADIUS_SMALL.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(Constants.ICON_SIZE_MEDIUM.dp),
                                strokeWidth = 2.dp,
                                color = NoteTheme.Primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    Card(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                // Handle back navigation with confirmation
                                if (hasContent && !isEditing) {
                                    showBackDialog = true
                                } else {
                                    navController.navigateUp()
                                }
                            },
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = NoteTheme.Primary.copy(alpha = 0.1f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Removed shadow
                    ) {
                        Box(
                            modifier = Modifier.padding(Constants.PADDING_SMALL.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = NoteTheme.Primary,
                                modifier = Modifier.size(Constants.ICON_SIZE_LARGE.dp)
                            )
                        }
                    }
                },
                actions = {
                    if (isEditing) {
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
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(Constants.PADDING_SMALL.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Delete",
                                    tint = NoteTheme.Error,
                                    modifier = Modifier.size(Constants.ICON_SIZE_LARGE.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            // Only show save button when there's no error message and not loading
            AnimatedVisibility(
                visible = !isLoading && canSave,
                enter = scaleIn(
                    animationSpec = UIUtils.getSpringAnimationSpec()
                ) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                var fabPressed by remember { mutableStateOf(false) }
                val fabScale by animateFloatAsState(
                    targetValue = if (fabPressed) 0.9f else 1f,
                    animationSpec = UIUtils.getSpringAnimationSpec(),
                    label = "fab_scale"
                )

                ExtendedFloatingActionButton(
                    onClick = {
                        fabPressed = true
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        saveNote()
                    },
                    modifier = Modifier
                        .scale(fabScale)
                        .shadow(Constants.CORNER_RADIUS_SMALL.dp, RoundedCornerShape(Constants.CORNER_RADIUS_LARGE.dp)),
                    containerColor = NoteTheme.Primary,
                    contentColor = NoteTheme.OnPrimary,
                    shape = RoundedCornerShape(Constants.CORNER_RADIUS_LARGE.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(Constants.ICON_SIZE_MEDIUM.dp),
                            strokeWidth = 2.dp,
                            color = NoteTheme.OnPrimary
                        )
                        Spacer(modifier = Modifier.width(Constants.CORNER_RADIUS_SMALL.dp))
                        Text(
                            "Saving...",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else {
                        Icon(
                            Icons.Outlined.Check,
                            contentDescription = "Save",
                            modifier = Modifier.size(Constants.ICON_SIZE_LARGE.dp)
                        )
                        Spacer(modifier = Modifier.width(Constants.CORNER_RADIUS_SMALL.dp))
                        Text(
                            if (isEditing) "Update Note" else "Save Note",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                LaunchedEffect(fabPressed) {
                    if (fabPressed) {
                        kotlinx.coroutines.delay(Constants.SPRING_ANIMATION_DELAY.toLong())
                        fabPressed = false
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(Constants.CORNER_RADIUS_LARGE.dp),
                    colors = CardDefaults.cardColors(containerColor = NoteTheme.Surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(Constants.PADDING_XL.dp)
                    ) {
                        CircularProgressIndicator(
                            color = NoteTheme.Primary,
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(Constants.PADDING_MEDIUM.dp))
                        Text(
                            "Loading note...",
                            style = MaterialTheme.typography.titleMedium,
                            color = NoteTheme.OnSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .padding(paddingValues)
                    .padding(Constants.PADDING_MEDIUM.dp),
                verticalArrangement = Arrangement.spacedBy(Constants.CORNER_RADIUS_LARGE.dp)
            ) {
                // Title Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = NoteTheme.Surface
                    ),
                    shape = RoundedCornerShape(Constants.CORNER_RADIUS_LARGE.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(Constants.CORNER_RADIUS_LARGE.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
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

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Progress indicator
                                Box(
                                    modifier = Modifier
                                        .size(Constants.PROGRESS_INDICATOR_SIZE.dp)
                                        .background(
                                            color = titleCountColor.copy(alpha = 0.1f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        progress = { titleProgress },
                                        modifier = Modifier.fillMaxSize(),
                                        color = titleCountColor,
                                        strokeWidth = 2.dp
                                    )
                                }

                                Spacer(modifier = Modifier.width(Constants.PADDING_SMALL.dp))

                                Text(
                                    text = UIUtils.formatCharacterCount(title.length, Constants.TITLE_MAX_LENGTH),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = titleCountColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Constants.PADDING_MEDIUM.dp))

                        OutlinedTextField(
                            value = title,
                            onValueChange = { if (it.length <= Constants.TITLE_MAX_LENGTH) title = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(titleFocusRequester)
                                .onFocusChanged { titleFocused = it.isFocused },
                            placeholder = {
                                Text(
                                    "Enter a compelling title...",
                                    color = NoteTheme.OnSurfaceVariant
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = titleCountColor,
                                unfocusedBorderColor = NoteTheme.OnSurfaceVariant.copy(alpha = 0.3f),
                                cursorColor = NoteTheme.Primary,
                                focusedLabelColor = titleCountColor,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            shape = RoundedCornerShape(Constants.CORNER_RADIUS_SMALL.dp)
                        )

                        // Title requirements indicator
                        AnimatedVisibility(
                            visible = !ValidationUtils.isValidTitle(title),
                            enter = slideInVertically() + fadeIn(),
                            exit = slideOutVertically() + fadeOut()
                        ) {
                            Row(
                                modifier = Modifier.padding(top = Constants.PADDING_SMALL.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = titleCountColor,
                                    modifier = Modifier.size(Constants.ICON_SIZE_SMALL.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = ValidationUtils.getTitleValidationMessage(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = titleCountColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Description Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .animateContentSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = NoteTheme.Surface
                    ),
                    shape = RoundedCornerShape(Constants.CORNER_RADIUS_LARGE.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(Constants.CORNER_RADIUS_LARGE.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
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

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Progress indicator
                                Box(
                                    modifier = Modifier
                                        .size(Constants.PROGRESS_INDICATOR_SIZE.dp)
                                        .background(
                                            color = descCountColor.copy(alpha = 0.1f),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        progress = { descriptionProgress },
                                        modifier = Modifier.fillMaxSize(),
                                        color = descCountColor,
                                        strokeWidth = 2.dp
                                    )
                                }

                                Spacer(modifier = Modifier.width(Constants.PADDING_SMALL.dp))

                                Text(
                                    text = UIUtils.formatCharacterCount(description.length, Constants.DESCRIPTION_MAX_LENGTH),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = descCountColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Constants.PADDING_MEDIUM.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { if (it.length <= Constants.DESCRIPTION_MAX_LENGTH) description = it },
                            modifier = Modifier
                                .fillMaxSize()
                                .onFocusChanged { descriptionFocused = it.isFocused },
                            placeholder = {
                                Text(
                                    "Write your thoughts here...\n\nExpress your ideas, capture important information, or jot down anything that comes to mind.",
                                    color = NoteTheme.OnSurfaceVariant
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = descCountColor,
                                unfocusedBorderColor = NoteTheme.OnSurfaceVariant.copy(alpha = 0.3f),
                                cursorColor = NoteTheme.Secondary,
                                focusedLabelColor = descCountColor,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = { keyboardController?.hide() }
                            ),
                            shape = RoundedCornerShape(Constants.CORNER_RADIUS_SMALL.dp)
                        )
                    }
                }

                // Save requirements indicator - This ensures save button doesn't overlap
                AnimatedVisibility(
                    visible = !canSave,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = NoteTheme.Warning.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(Constants.CORNER_RADIUS_MEDIUM.dp),
                        border = BorderStroke(1.dp, NoteTheme.Warning.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(Constants.PADDING_MEDIUM.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = NoteTheme.Warning,
                                modifier = Modifier.size(Constants.ICON_SIZE_MEDIUM.dp)
                            )
                            Spacer(modifier = Modifier.width(Constants.CORNER_RADIUS_SMALL.dp))
                            Text(
                                text = ValidationUtils.getSaveValidationMessage(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = NoteTheme.Warning,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    // Auto-focus title when creating new note
    LaunchedEffect(Unit) {
        if (!isEditing && !isLoading) {
            kotlinx.coroutines.delay(Constants.LOADING_DELAY)
            titleFocusRequester.requestFocus()
        }
    }

    // Handle device back button
    BackHandler(enabled = true) {
        // Handle back navigation with confirmation for device back button
        if (hasContent && !isEditing) {
            showBackDialog = true
        } else {
            navController.navigateUp()
        }
    }
}
