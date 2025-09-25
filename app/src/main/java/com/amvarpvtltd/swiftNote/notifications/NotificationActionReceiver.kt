package com.amvarpvtltd.swiftNote.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.amvarpvtltd.swiftNote.reminders.ReminderEntity

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra("reminderId") ?: return
        val noteId = intent.getStringExtra("noteId")
        val noteTitle = intent.getStringExtra("noteTitle")
        val noteDescription = intent.getStringExtra("noteDescription")

        when (intent.action) {
            "MARK_DONE" -> {
                NotificationHelper(context).cancelNotification(reminderId)
                Toast.makeText(context, "Reminder marked as done", Toast.LENGTH_SHORT).show()
            }
            "SNOOZE" -> {
                NotificationHelper(context).cancelNotification(reminderId)
                if (noteId != null && noteTitle != null) {
                    val snoozeTime = System.currentTimeMillis() + 10 * 60 * 1000 // 10 min
                    val reminder = ReminderEntity(
                        id = "${reminderId}_snoozed_${System.currentTimeMillis()}",
                        noteId = noteId,
                        noteTitle = noteTitle,
                        noteDescription = noteDescription ?: "",
                        reminderTime = snoozeTime,
                        isActive = true
                    )
                    ReminderScheduler(context).scheduleReminder(reminder)
                    Toast.makeText(context, "Reminder snoozed for 10 minutes", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

