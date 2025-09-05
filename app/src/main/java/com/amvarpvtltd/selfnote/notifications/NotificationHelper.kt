package com.amvarpvtltd.selfnote.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.amvarpvtltd.selfnote.MainActivity
import com.amvarpvtltd.selfnote.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "note_reminders"
        const val CHANNEL_NAME = "Note Reminders"
        const val CHANNEL_DESCRIPTION = "Notifications for note reminders"
        const val NOTIFICATION_ID_BASE = 1000
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
            enableVibration(true)
            enableLights(true)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showReminderNotification(
        reminderId: String,
        noteId: String,
        noteTitle: String,
        noteDescription: String
    ) {
        if (!hasNotificationPermission()) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("noteId", noteId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            noteId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Mark as Done
        val markDoneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "MARK_DONE"
            putExtra("reminderId", reminderId)
        }
        val markDonePendingIntent = PendingIntent.getBroadcast(
            context,
            (reminderId + "_done").hashCode(),
            markDoneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze 10 min
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "SNOOZE"
            putExtra("reminderId", reminderId)
            putExtra("noteId", noteId)
            putExtra("noteTitle", noteTitle)
            putExtra("noteDescription", noteDescription)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (reminderId + "_snooze").hashCode(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo2) // Use latest app logo
            .setLargeIcon(android.graphics.BitmapFactory.decodeResource(context.resources, R.drawable.logo2))
            .setContentTitle("📝 Reminder: $noteTitle")
            .setContentText(if (noteDescription.isNotEmpty()) noteDescription else "Tap to view your note")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(if (noteDescription.isNotEmpty()) noteDescription else "Tap to view your note")
                .setBigContentTitle("📝 Reminder: $noteTitle")
                .setSummaryText("QuickNote Reminder"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(androidx.core.content.ContextCompat.getColor(context, com.amvarpvtltd.selfnote.R.color.purple_500))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .addAction(android.R.drawable.ic_menu_agenda, "Done", markDonePendingIntent)
            .addAction(android.R.drawable.ic_popup_reminder, "Snooze 10 min", snoozePendingIntent)
            .setOngoing(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        val notificationId = NOTIFICATION_ID_BASE + reminderId.hashCode()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            android.util.Log.e("NotificationHelper", "Permission denied for notifications", e)
            android.widget.Toast.makeText(
                context,
                "Notification permission required for reminders",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun cancelNotification(reminderId: String) {
        val notificationId = NOTIFICATION_ID_BASE + reminderId.hashCode()
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }
}
