package com.amvarpvtltd.swiftNote.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amvarpvtltd.swiftNote.design.NoteTheme
import com.amvarpvtltd.swiftNote.utils.Constants
import kotlinx.coroutines.delay

/**
 * Different types of notifications with their corresponding styling
 */
enum class NotificationType {
    SUCCESS, ERROR, WARNING, INFO
}

/**
 * Data class representing the notification content and configuration
 */
data class NotificationData(
    val title: String,
    val message: String,
    val type: NotificationType = NotificationType.INFO,
    val duration: Long = 3000 // Default duration in milliseconds
)

/**
 * Singleton object to manage notifications across the app
 */
object NotificationManager {
    private val _currentNotification = mutableStateOf<NotificationData?>(null)
    val currentNotification: MutableState<NotificationData?> = _currentNotification

    /**
     * Shows a notification with the provided data
     */
    fun showNotification(
        title: String,
        message: String,
        type: NotificationType = NotificationType.INFO,
        duration: Long = 3000
    ) {
        _currentNotification.value = NotificationData(
            title = title,
            message = message,
            type = type,
            duration = duration
        )
    }

    /**
     * Dismisses the current notification
     */
    fun dismissNotification() {
        _currentNotification.value = null
    }
}

/**
 * A reusable notification component with different types and automatic dismissal
 */
@Composable
fun Notification(
    data: NotificationData,
    onDismiss: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    // Determine colors and icon based on type
    val (backgroundColor, contentColor, icon) = when (data.type) {
        NotificationType.SUCCESS -> Triple(
            NoteTheme.SecondaryContainer,
            NoteTheme.OnSecondaryContainer,
            Icons.Filled.CheckCircle
        )
        NotificationType.ERROR -> Triple(
            Color(0xFFFEE2E2), // Light red background
            Color(0xFF991B1B), // Dark red text
            Icons.Filled.ErrorOutline
        )
        NotificationType.WARNING -> Triple(
            Color(0xFFFEF3C7), // Light amber background
            Color(0xFF92400E), // Dark amber text
            Icons.Filled.Warning
        )
        NotificationType.INFO -> Triple(
            NoteTheme.PrimaryContainer,
            NoteTheme.OnPrimaryContainer,
            Icons.Filled.Info
        )
    }

    // Auto-dismiss effect
    LaunchedEffect(data) {
        isVisible = true
        delay(data.duration)
        isVisible = false
        delay(300) // Allow exit animation to play
        onDismiss()
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300)) + expandVertically(
            animationSpec = tween(300),
            expandFrom = Alignment.Top
        ),
        exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(
            animationSpec = tween(300),
            shrinkTowards = Alignment.Top
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Constants.PADDING_LARGE.dp, vertical = Constants.PADDING_MEDIUM.dp)
                .clip(RoundedCornerShape(Constants.CORNER_RADIUS_LARGE.dp))
                .border(
                    width = 1.dp,
                    color = contentColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(Constants.CORNER_RADIUS_LARGE.dp)
                ),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Constants.PADDING_MEDIUM.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(Constants.CORNER_RADIUS_MEDIUM.dp))
                        .background(contentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(Constants.PADDING_MEDIUM.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Text(
                        text = data.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }

                IconButton(
                    onClick = {
                        isVisible = false
                        // Use a coroutine to delay the actual dismissal until the animation completes
                        onDismiss()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Dismiss",
                        tint = contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Host component that displays the current notification
 * Place this at the top level of your app's UI hierarchy
 */
@Composable
fun NotificationHost() {
    val notification = NotificationManager.currentNotification.value
    notification?.let {
        Notification(
            data = it,
            onDismiss = {
                NotificationManager.dismissNotification()
            }
        )
    }
}

/**
 * Helper extension functions for common notification types
 */
object NotificationHelper {
    fun showSuccess(
        title: String,
        message: String,
        duration: Long = 3000
    ) {
        NotificationManager.showNotification(
            title = title,
            message = message,
            type = NotificationType.SUCCESS,
            duration = duration
        )
    }

    fun showError(
        title: String,
        message: String,
        duration: Long = 4000
    ) {
        NotificationManager.showNotification(
            title = title,
            message = message,
            type = NotificationType.ERROR,
            duration = duration
        )
    }

    fun showWarning(
        title: String,
        message: String,
        duration: Long = 3500
    ) {
        NotificationManager.showNotification(
            title = title,
            message = message,
            type = NotificationType.WARNING,
            duration = duration
        )
    }

    fun showInfo(
        title: String,
        message: String,
        duration: Long = 3000
    ) {
        NotificationManager.showNotification(
            title = title,
            message = message,
            type = NotificationType.INFO,
            duration = duration
        )
    }
}