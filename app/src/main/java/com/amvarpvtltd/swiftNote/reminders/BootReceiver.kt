package com.amvarpvtltd.swiftNote.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Boot receiver to reschedule reminders after device restart
 * Compliant with Play Store background execution policies
 */
class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                // Only reschedule if the replaced package is our app
                if (intent.action == Intent.ACTION_PACKAGE_REPLACED) {
                    val packageName = intent.dataString
                    if (!packageName?.contains(context.packageName) == true) {
                        return
                    }
                }
                
                rescheduleReminders(context)
            }
        }
    }
    
    private fun rescheduleReminders(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminderManager = ReminderManager(context)
                reminderManager.rescheduleAllReminders()
            } catch (e: Exception) {
                // Handle silently as per Play Store policies
                // In a production app, you might want to log this for debugging
            }
        }
    }
}