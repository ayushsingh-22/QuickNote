package com.amvarpvtltd.selfnote.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amvarpvtltd.selfnote.design.NoteTheme
import com.amvarpvtltd.selfnote.utils.Constants
import kotlinx.coroutines.delay

@Composable
fun OfflineBanner(
    isVisible: Boolean,
    message: String = "You're offline. Changes will sync when connected.",
    onDismiss: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showPulse by remember { mutableStateOf(true) }

    val pulseAlpha by animateFloatAsState(
        targetValue = if (showPulse) 0.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    LaunchedEffect(isVisible) {
        if (isVisible) {
            while (isVisible) {
                showPulse = true
                delay(1500)
                showPulse = false
                delay(1500)
            }
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Constants.PADDING_MEDIUM.dp),
            colors = CardDefaults.cardColors(
                containerColor = NoteTheme.Warning.copy(alpha = pulseAlpha)
            ),
            shape = RoundedCornerShape(Constants.CORNER_RADIUS_MEDIUM.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Constants.PADDING_MEDIUM.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudOff,
                        contentDescription = null,
                        tint = NoteTheme.OnSurface,
                        modifier = Modifier.size(Constants.ICON_SIZE_MEDIUM.dp)
                    )
                    Spacer(modifier = Modifier.width(Constants.PADDING_SMALL.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NoteTheme.OnSurface,
                        fontWeight = FontWeight.Medium
                    )
                }

                onDismiss?.let {
                    IconButton(onClick = it) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Dismiss",
                            tint = NoteTheme.OnSurface,
                            modifier = Modifier.size(Constants.ICON_SIZE_SMALL.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineActionDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    title: String = "You're Offline",
    message: String = "This action requires an internet connection. Your data will sync automatically when you're back online.",
    actionText: String = "Continue Offline",
    onActionClick: (() -> Unit)? = null,
    icon: ImageVector = Icons.Outlined.CloudOff
) {
    val hapticFeedback = LocalHapticFeedback.current

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
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
                        imageVector = icon,
                        contentDescription = null,
                        tint = NoteTheme.Warning,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = NoteTheme.OnSurface,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = NoteTheme.OnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(Constants.PADDING_MEDIUM.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = NoteTheme.SurfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(Constants.CORNER_RADIUS_SMALL.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(Constants.PADDING_MEDIUM.dp),
                            verticalArrangement = Arrangement.spacedBy(Constants.PADDING_SMALL.dp)
                        ) {
                            Text(
                                text = "Offline Features:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = NoteTheme.OnSurface
                            )

                            listOf(
                                "Create and edit notes",
                                "View existing notes",
                                "Delete notes",
                                "Search notes"
                            ).forEach { feature ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null,
                                        tint = NoteTheme.Success,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = feature,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NoteTheme.OnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (onActionClick != null) {
                    Button(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onActionClick()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NoteTheme.Primary,
                            contentColor = NoteTheme.OnPrimary
                        ),
                        shape = RoundedCornerShape(Constants.CORNER_RADIUS_SMALL.dp)
                    ) {
                        Text(actionText, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NoteTheme.OnSurfaceVariant
                    ),
                    shape = RoundedCornerShape(Constants.CORNER_RADIUS_SMALL.dp)
                ) {
                    Text("OK", fontWeight = FontWeight.Medium)
                }
            },
            shape = RoundedCornerShape(Constants.CORNER_RADIUS_XL.dp),
            containerColor = NoteTheme.Surface
        )
    }
}

@Composable
fun OfflineIndicator(
    isOnline: Boolean,
    modifier: Modifier = Modifier,
    showText: Boolean = true
) {
    AnimatedVisibility(
        visible = !isOnline,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = NoteTheme.Error.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(Constants.CORNER_RADIUS_SMALL.dp)
        ) {
            Row(
                modifier = Modifier.padding(Constants.PADDING_SMALL.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CloudOff,
                    contentDescription = "Offline",
                    tint = NoteTheme.Error,
                    modifier = Modifier.size(Constants.ICON_SIZE_SMALL.dp)
                )

                if (showText) {
                    Text(
                        text = "Offline",
                        style = MaterialTheme.typography.labelSmall,
                        color = NoteTheme.Error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun rememberOfflineHandler(
    isOnline: Boolean,
    onOfflineAction: () -> Unit = {}
): OfflineHandler {
    return remember(isOnline) {
        OfflineHandler(isOnline, onOfflineAction)
    }
}

class OfflineHandler(
    private val isOnline: Boolean,
    private val onOfflineAction: () -> Unit
) {
    fun executeIfOnline(action: () -> Unit) {
        if (isOnline) {
            action()
        } else {
            onOfflineAction()
        }
    }
}
