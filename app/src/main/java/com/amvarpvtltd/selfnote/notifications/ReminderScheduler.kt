package com.amvarpvtltd.selfnote.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.*
import com.amvarpvtltd.selfnote.reminders.ReminderEntity
import java.util.concurrent.TimeUnit

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationHelper = NotificationHelper(context)

    fun scheduleReminder(reminder: ReminderEntity) {
        val currentTime = System.currentTimeMillis()

        if (reminder.reminderTime <= currentTime) {
            Log.w("ReminderScheduler", "Cannot schedule reminder in the past")
            return
        }

        // Use WorkManager for reliable background execution
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(reminder.reminderTime - currentTime, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(
                "reminderId" to reminder.id,
                "noteId" to reminder.noteId,
                "noteTitle" to reminder.noteTitle,
                "noteDescription" to reminder.noteDescription
            ))
            .addTag("reminder_${reminder.id}")
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)

        Log.d("ReminderScheduler", "Scheduled reminder ${reminder.id} for ${reminder.noteTitle}")
    }

    fun cancelReminder(reminderId: String) {
        // Cancel WorkManager task
        WorkManager.getInstance(context).cancelAllWorkByTag("reminder_$reminderId")

        // Cancel any existing notification
        notificationHelper.cancelNotification(reminderId)

        Log.d("ReminderScheduler", "Cancelled reminder $reminderId")
    }

    fun rescheduleAllReminders(reminders: List<ReminderEntity>) {
        val currentTime = System.currentTimeMillis()

        reminders.filter { it.isActive && it.reminderTime > currentTime }
            .forEach { scheduleReminder(it) }
    }
}

class ReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val reminderId = inputData.getString("reminderId") ?: return Result.failure()
        val noteId = inputData.getString("noteId") ?: return Result.failure()
        val noteTitle = inputData.getString("noteTitle") ?: return Result.failure()
        val noteDescription = inputData.getString("noteDescription") ?: ""

        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.showReminderNotification(reminderId, noteId, noteTitle, noteDescription)

        Log.d("ReminderWorker", "Showed reminder notification for note: $noteTitle")

        return Result.success()
    }
}
