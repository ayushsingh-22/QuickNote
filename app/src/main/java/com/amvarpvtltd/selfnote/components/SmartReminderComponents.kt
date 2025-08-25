package com.amvarpvtltd.selfnote.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amvarpvtltd.selfnote.ai.DetectedReminder
import com.amvarpvtltd.selfnote.design.NoteTheme
import com.amvarpvtltd.selfnote.reminders.ReminderManager
import com.amvarpvtltd.selfnote.utils.Constants
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Individual reminder suggestion card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderSuggestionCard(
    reminder: DetectedReminder,
    index: Int,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current

    AnimatedVisibility(
        visible = true,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(300, delayMillis = index * 50)
        ) + fadeIn(animationSpec = tween(300, delayMillis = index * 50))
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(),
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onSelect()
            },
            colors = CardDefaults.cardColors(
                containerColor = NoteTheme.Surface
            ),
            shape = RoundedCornerShape(Constants.CORNER_RADIUS_MEDIUM.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Constants.PADDING_MEDIUM.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon based on reminder type
                val (icon, iconColor) = getReminderIconAndColor(reminder.extractedText)

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            iconColor.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(Constants.PADDING_SMALL.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = NoteTheme.OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = formatDateTime(reminder.reminderDateTime),
                        style = MaterialTheme.typography.bodySmall,
                        color = NoteTheme.Primary,
                        fontWeight = FontWeight.Medium
                    )

                    if (reminder.extractedText.isNotBlank()) {
                        Text(
                            text = "\"${reminder.extractedText}\"",
                            style = MaterialTheme.typography.bodySmall,
                            color = NoteTheme.OnSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }

                // Confidence indicator
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            getConfidenceColor(reminder.confidence).copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${(reminder.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = getConfidenceColor(reminder.confidence),
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}

/**
 * Confirmation dialog for creating a reminder
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderConfirmationDialog(
    reminder: DetectedReminder,
    isCreating: Boolean,
    onConfirm: (DetectedReminder) -> Unit,
    onDismiss: () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current
    val reminderManager = remember { ReminderManager.getInstance(context) }

    // Check if we can schedule exact alarms
    val canScheduleAlarms = reminderManager.canScheduleExactAlarms()

    AlertDialog(
        onDismissRequest = { if (!isCreating) onDismiss() },
        icon = {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NoteTheme.Primary.copy(alpha = 0.3f),
                                NoteTheme.Primary.copy(alpha = 0.1f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = NoteTheme.Primary,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Text(
                text = "Create Smart Reminder?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = NoteTheme.OnSurface,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column {
                if (!canScheduleAlarms) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = NoteTheme.Warning.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(Constants.CORNER_RADIUS_SMALL.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(Constants.PADDING_MEDIUM.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = NoteTheme.Warning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Exact alarm permission required for precise reminders",
                                style = MaterialTheme.typography.bodySmall,
                                color = NoteTheme.Warning,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Constants.PADDING_MEDIUM.dp))
                }

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = NoteTheme.Surface
                    ),
                    shape = RoundedCornerShape(Constants.CORNER_RADIUS_MEDIUM.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(Constants.PADDING_MEDIUM.dp)
                    ) {
                        Text(
                            text = reminder.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NoteTheme.OnSurface
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = formatDateTime(reminder.reminderDateTime),
                            style = MaterialTheme.typography.bodyMedium,
                            color = NoteTheme.Primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (reminder.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = reminder.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = NoteTheme.OnSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Psychology,
                                contentDescription = null,
                                tint = NoteTheme.Secondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AI Confidence: ${(reminder.confidence * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = NoteTheme.Secondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Constants.PADDING_SMALL.dp))

                Text(
                    text = "Would you like to create this reminder? You'll receive a notification at the specified time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NoteTheme.OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (!canScheduleAlarms) {
                        reminderManager.requestExactAlarmPermission()
                    } else {
                        onConfirm(reminder)
                    }
                },
                enabled = !isCreating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NoteTheme.Primary,
                    contentColor = NoteTheme.OnPrimary
                ),
                shape = RoundedCornerShape(Constants.CORNER_RADIUS_SMALL.dp)
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = NoteTheme.OnPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Creating...")
                } else {
                    Icon(
                        if (canScheduleAlarms) Icons.Outlined.Schedule else Icons.Outlined.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (canScheduleAlarms) "Create Reminder" else "Grant Permission",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isCreating,
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

/**
 * Helper functions
 */
private fun getReminderIconAndColor(extractedText: String): Pair<ImageVector, androidx.compose.ui.graphics.Color> {
    val text = extractedText.lowercase()
    return when {
        text.contains("appointment") || text.contains("doctor") -> Icons.Outlined.LocalHospital to NoteTheme.Error
        text.contains("meeting") -> Icons.Outlined.Groups to NoteTheme.Primary
        text.contains("call") -> Icons.Outlined.Phone to NoteTheme.Secondary
        text.contains("deadline") -> Icons.Outlined.Schedule to NoteTheme.Warning
        text.contains("birthday") -> Icons.Outlined.Cake to NoteTheme.Success
        text.contains("event") -> Icons.Outlined.Event to NoteTheme.Primary
        else -> Icons.Outlined.NotificationsActive to NoteTheme.Secondary
    }
}

private fun getConfidenceColor(confidence: Float): androidx.compose.ui.graphics.Color {
    return when {
        confidence >= 0.8f -> NoteTheme.Success
        confidence >= 0.6f -> NoteTheme.Warning
        else -> NoteTheme.Error
    }
}

private fun formatDateTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = timestamp - now

    val formatter = when {
        diff < 24 * 60 * 60 * 1000 -> SimpleDateFormat("'Today at' hh:mm a", Locale.getDefault())
        diff < 2 * 24 * 60 * 60 * 1000 -> SimpleDateFormat("'Tomorrow at' hh:mm a", Locale.getDefault())
        else -> SimpleDateFormat("MMM dd 'at' hh:mm a", Locale.getDefault())
    }

    return formatter.format(Date(timestamp))
}
