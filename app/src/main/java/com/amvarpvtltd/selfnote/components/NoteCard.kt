package com.amvarpvtltd.selfnote.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.amvarpvtltd.selfnote.dataclass
import com.amvarpvtltd.selfnote.design.NoteTheme
import com.halilibo.richtext.markdown.Markdown
import com.halilibo.richtext.ui.material3.Material3RichText
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCard(
    note: dataclass,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onShare: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onReminder: (() -> Unit)? = null
) {
    val hapticFeedback = LocalHapticFeedback.current
    val colorIndex = Math.abs(note.hashCode()) % noteCardColors.size
    val cardColors = noteCardColors[colorIndex]
    val backgroundColor = cardColors.first
    val accentColor = cardColors.second

    var isPressed by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp, max = 180.dp) // More compact height
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .shadow(
                elevation = if (isPressed) 8.dp else 4.dp,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Accent color strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(accentColor.copy(alpha = 0.7f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Content Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    // Title with proper truncation
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = NoteTheme.OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Description preview with markdown
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Material3RichText(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Markdown(
                                content = if (note.description.length > 150) {
                                    note.description.take(150) + "..."
                                } else {
                                    note.description
                                }
                            )
                        }
                    }
                }
            }

            // Action buttons in a bottom bar
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = accentColor.copy(alpha = 0.1f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Edit button
                IconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onEdit?.invoke()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Share button
                IconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onShare?.invoke()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Reminder button
                IconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onReminder?.invoke()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = "Set Reminder",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Delete button
                IconButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDelete?.invoke()
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = NoteTheme.Error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}