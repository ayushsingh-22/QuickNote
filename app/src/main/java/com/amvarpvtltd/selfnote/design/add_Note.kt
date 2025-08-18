package com.amvarpvtltd.selfnote.design

import android.util.Log
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
import com.google.firebase.database.FirebaseDatabase
import dataclass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import myGlobalMobileDeviceId
import androidx.compose.material3.ButtonDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(navController: NavHostController, noteId: String?) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var titleFocused by remember { mutableStateOf(false) }
    var descriptionFocused by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance()
    val notesRef = database.getReference("notes").child(myGlobalMobileDeviceId)
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val titleFocusRequester = remember { FocusRequester() }
    val hapticFeedback = LocalHapticFeedback.current

    val isEditing = noteId != null
    val canSave = title.trim().length >= 5 || description.trim().length >= 5

    // Animated colors and values
    val animatedSaveColor by animateColorAsState(
        targetValue = if (canSave) NoteTheme.Primary else NoteTheme.OnSurfaceVariant,
        animationSpec = tween(300),
        label = "save_color"
    )

    val titleProgress = (title.length / 50f).coerceAtMost(1f)
    val descriptionProgress = (description.length / 1000f).coerceAtMost(1f)

    val titleCountColor by animateColorAsState(
        targetValue = when {
            title.length >= 5 -> NoteTheme.Success
            title.length >= 3 -> NoteTheme.Warning
            else -> NoteTheme.Error
        },
        animationSpec = tween(300),
        label = "title_count_color"
    )

    val descCountColor by animateColorAsState(
        targetValue = when {
            description.length >= 5 -> NoteTheme.Success
            description.length >= 3 -> NoteTheme.Warning
            else -> NoteTheme.Error
        },
        animationSpec = tween(300),
        label = "desc_count_color"
    )

    // Load existing note data
    LaunchedEffect(noteId) {
        if (noteId != null) {
            isLoading = true
            try {
                val query = notesRef.orderByChild("id").equalTo(noteId)
                val snapshot = query.get().await()

                if (snapshot.exists()) {
                    val keyToUpdate = snapshot.children.first().key
                    if (keyToUpdate != null) {
                        val noteSnapshot = notesRef.child(keyToUpdate).get().await()
                        val encryptedNote = noteSnapshot.getValue(dataclass::class.java)
                        encryptedNote?.let {
                            // Decrypt the note data before displaying
                            val decryptedNote = dataclass.fromEncryptedData(it)
                            title = decryptedNote.title
                            description = decryptedNote.description
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("Firebase", "Error fetching note", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Error loading note", Toast.LENGTH_LONG).show()
                }
            } finally {
                isLoading = false
            }
        }
    }

    // Save note function
    fun saveNote() {
        if (!canSave) {
            Toast.makeText(
                context,
                "⚠️ Title or description must be at least 5 characters",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        isSaving = true
        val note = dataclass(title = title.trim(), description = description.trim())
        note.mymobiledeviceid = myGlobalMobileDeviceId

        scope.launch(Dispatchers.IO) {
            try {
                val deviceIdRef = database.getReference("notes").child(myGlobalMobileDeviceId)

                if (noteId == null) {
                    // Save encrypted data to Firebase
                    val encryptedNote = note.toEncryptedData()
                    deviceIdRef.push().setValue(encryptedNote).await()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "✅ Note saved successfully!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val query = notesRef.orderByChild("id").equalTo(noteId)
                    val keyToUpdate = query.get().await().children.firstOrNull()?.key
                    note.id = noteId
                    // Save encrypted data to Firebase
                    val encryptedNote = note.toEncryptedData()
                    deviceIdRef.child(keyToUpdate!!).setValue(encryptedNote).await()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "✅ Note updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                }

                withContext(Dispatchers.Main) {
                    navController.navigate("noteScreen")
                }
            } catch (e: Exception) {
                Log.e("Firebase", "Error saving note", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Error saving note", Toast.LENGTH_LONG).show()
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
                val query = notesRef.orderByChild("id").equalTo(noteId)
                val keyToDelete = query.get().await().children.firstOrNull()?.key

                if (keyToDelete != null) {
                    notesRef.child(keyToDelete).removeValue().await()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "🗑️ Note deleted successfully", Toast.LENGTH_SHORT).show()
                        navController.navigate("noteScreen")
                    }
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "❌ Error deleting note", Toast.LENGTH_LONG).show()
                }
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
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "\"$title\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NoteTheme.OnSurface,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(12.dp)
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
                    shape = RoundedCornerShape(12.dp)
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
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = NoteTheme.Surface
        )
    }

    Scaffold(
        modifier = Modifier.background(backgroundBrush),
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
                            Spacer(modifier = Modifier.width(12.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = NoteTheme.Primary
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
                                modifier = Modifier.padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.Delete,
                                    contentDescription = "Delete",
                                    tint = NoteTheme.Error,
                                    modifier = Modifier.size(24.dp)
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
            AnimatedVisibility(
                visible = !isLoading,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy
                    )
                ) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                var fabPressed by remember { mutableStateOf(false) }
                val fabScale by animateFloatAsState(
                    targetValue = if (fabPressed) 0.9f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
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
                        .shadow(12.dp, RoundedCornerShape(20.dp)),
                    containerColor = animatedSaveColor,
                    contentColor = NoteTheme.OnPrimary,
                    shape = RoundedCornerShape(20.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = NoteTheme.OnPrimary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Saving...",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    } else {
                        Icon(
                            Icons.Outlined.Check,
                            contentDescription = "Save",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            if (isEditing) "Update Note" else "Save Note",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                LaunchedEffect(fabPressed) {
                    if (fabPressed) {
                        kotlinx.coroutines.delay(150)
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
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = NoteTheme.Surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        CircularProgressIndicator(
                            color = NoteTheme.Primary,
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
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
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Title Section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (titleFocused) 8.dp else 4.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = NoteTheme.Surface
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
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
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
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
                                        .size(24.dp)
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

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "${title.length}/50",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = titleCountColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = title,
                            onValueChange = { if (it.length <= 50) title = it },
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
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Title requirements indicator
                        AnimatedVisibility(
                            visible = title.length < 5,
                            enter = slideInVertically() + fadeIn(),
                            exit = slideOutVertically() + fadeOut()
                        ) {
                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = titleCountColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Title must be at least 5 characters",
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
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (descriptionFocused) 8.dp else 4.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = NoteTheme.Surface
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
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
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))

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
                                        .size(24.dp)
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

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "${description.length}/1000",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = descCountColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = description,
                            onValueChange = { if (it.length <= 1000) description = it },
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
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                // Save requirements indicator
                AnimatedVisibility(
                    visible = !canSave,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = NoteTheme.Warning.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, NoteTheme.Warning.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = NoteTheme.Warning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Either title or description must be at least 5 characters to save",
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
            kotlinx.coroutines.delay(300)
            titleFocusRequester.requestFocus()
        }
    }
}
