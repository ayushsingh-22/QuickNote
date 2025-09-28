package com.amvarpvtltd.swiftNote.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.amvarpvtltd.swiftNote.MainActivity
import com.amvarpvtltd.swiftNote.R
import com.amvarpvtltd.swiftNote.room.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Broadcast receiver for handling reminder notifications
 * Compliant with Play Store policies for background processing
 */
class ReminderReceiver : BroadcastReceiver() {
    
    companion object {
        const val CHANNEL_ID = "reminder_notifications"
        const val CHANNEL_NAME = "Smart Reminders"
        const val CHANNEL_DESCRIPTION = "Notifications for your note reminders"
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_NOTE_ID = "note_id"
        const val EXTRA_TITLE = "reminder_title"
        const val EXTRA_MESSAGE = "reminder_message"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1)
        val noteId = intent.getStringExtra(EXTRA_NOTE_ID) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Reminder"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "You have a reminder"
        
        if (reminderId == -1L) return
        
        // Create notification channel if needed
        createNotificationChannel(context)
        
        // Show notification
        showReminderNotification(context, reminderId, noteId, title, message)
        
        // Mark reminder as completed in database
        markReminderCompleted(context, reminderId)
    }
    
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun showReminderNotification(
        context: Context,
        reminderId: Long,
        noteId: String,
        title: String,
        message: String
    ) {
        // Create intent to open the specific note
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("noteId", noteId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo2) // Use app icon
            .setContentTitle("📝 $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()
        
        // Show notification with proper permissions check
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(reminderId.toInt(), notification)
            }
        } catch (e: SecurityException) {
            // Handle case where notification permission is not granted
            // This is a silent fail as per Play Store policies
        }
    }
    
    private fun markReminderCompleted(context: Context, reminderId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val reminder = database.reminderDao().getReminderById(reminderId)
                reminder?.let {
                    val updatedReminder = it.copy(isCompleted = true)
                    database.reminderDao().updateReminder(updatedReminder)
                }
            } catch (e: Exception) {
                // Handle database error silently
            }
        }
    }
}