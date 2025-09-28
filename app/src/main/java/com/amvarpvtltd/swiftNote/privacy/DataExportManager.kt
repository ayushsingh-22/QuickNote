package com.amvarpvtltd.swiftNote.privacy

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.amvarpvtltd.swiftNote.room.AppDatabase
import com.amvarpvtltd.swiftNote.room.NoteEntityMapper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Data Export Manager for GDPR compliance
 * Allows users to export all their data in a readable format
 */
class DataExportManager(private val context: Context) {
    
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .create()
    
    data class ExportData(
        val exportInfo: ExportInfo,
        val notes: List<ExportNote>,
        val reminders: List<ExportReminder>,
        val preferences: Map<String, String>,
        val privacy: PrivacyData
    )
    
    data class ExportInfo(
        val appVersion: String,
        val exportDate: String,
        val exportFormat: String = "JSON",
        val totalNotes: Int,
        val totalReminders: Int,
        val disclaimer: String = "This export contains all your personal data from SwiftNote. Keep it secure."
    )
    
    data class ExportNote(
        val id: String,
        val title: String,
        val content: String,
        val createdDate: String,
        val modifiedDate: String,
        val isEncrypted: Boolean
    )
    
    data class ExportReminder(
        val id: String,
        val noteId: String,
        val title: String,
        val message: String,
        val scheduledTime: String,
        val isCompleted: Boolean,
        val createdDate: String
    )
    
    data class PrivacyData(
        val consentVersion: Int,
        val privacyPolicyAccepted: Boolean,
        val dataCollectionConsent: Map<String, Boolean>,
        val lastUpdated: String
    )
    
    /**
     * Export all user data to a JSON file
     */
    suspend fun exportAllData(): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context)
            val consentManager = ConsentManager.getInstance(context)
            
            // Collect all data
            val notes = db.noteDao().getAllNotes().map { noteEntity ->
                val dataclass = NoteEntityMapper.toDataClass(noteEntity)
                ExportNote(
                    id = dataclass.id,
                    title = dataclass.title,
                    content = dataclass.description,
                    createdDate = formatTimestamp(dataclass.timestamp),
                    modifiedDate = formatTimestamp(dataclass.timestamp), // TODO: Add modified timestamp to data model
                    isEncrypted = true // All notes are encrypted
                )
            }
            
            val reminders = db.reminderDao().getAllReminders().map { reminder ->
                ExportReminder(
                    id = reminder.id.toString(),
                    noteId = reminder.noteId,
                    title = reminder.title,
                    message = reminder.message,
                    scheduledTime = formatTimestamp(reminder.scheduledTime),
                    isCompleted = reminder.isCompleted,
                    createdDate = formatTimestamp(reminder.createdAt)
                )
            }
            
            // Collect preferences
            val preferences = collectUserPreferences()
            
            // Privacy data
            val privacyData = PrivacyData(
                consentVersion = consentManager.consentState.value.consentVersion,
                privacyPolicyAccepted = consentManager.consentState.value.privacyPolicyAccepted,
                dataCollectionConsent = mapOf(
                    "notifications" to consentManager.hasNotificationConsent(),
                    "camera" to consentManager.hasCameraConsent(),
                    "sync" to consentManager.hasSyncConsent(),
                    "analytics" to consentManager.hasAnalyticsConsent()
                ),
                lastUpdated = getCurrentTimestamp()
            )
            
            val exportData = ExportData(
                exportInfo = ExportInfo(
                    appVersion = getAppVersion(),
                    exportDate = getCurrentTimestamp(),
                    totalNotes = notes.size,
                    totalReminders = reminders.size
                ),
                notes = notes,
                reminders = reminders,
                preferences = preferences,
                privacy = privacyData
            )
            
            // Write to file
            val fileName = "swiftnote_data_export_${getDateString()}.json"
            val file = File(context.cacheDir, fileName)
            file.writeText(gson.toJson(exportData))
            
            // Create file URI
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            
            Result.success(uri)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Share exported data with user
     */
    fun shareExportedData(uri: Uri): Intent {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "SwiftNote Data Export")
            putExtra(Intent.EXTRA_TEXT, "Your SwiftNote data export. This file contains all your notes, reminders, and preferences.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        return Intent.createChooser(shareIntent, "Export SwiftNote Data")
    }
    
    /**
     * Export data as readable text format
     */
    suspend fun exportAsReadableText(): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            
            val stringBuilder = StringBuilder()
            stringBuilder.append("SWIFTNOTE DATA EXPORT\n")
            stringBuilder.append("Generated on: ${getCurrentTimestamp()}\n")
            stringBuilder.append("=" + "=".repeat(50) + "\n\n")
            
            // Export notes
            val notes = db.noteDao().getAllNotes()
            stringBuilder.append("NOTES (${notes.size} total)\n")
            stringBuilder.append("-".repeat(30) + "\n\n")
            
            notes.forEach { noteEntity ->
                val dataclass = NoteEntityMapper.toDataClass(noteEntity)
                stringBuilder.append("Title: ${dataclass.title}\n")
                stringBuilder.append("Created: ${formatTimestamp(dataclass.timestamp)}\n")
                stringBuilder.append("Content:\n${dataclass.description}\n")
                stringBuilder.append("\n" + "-".repeat(30) + "\n\n")
            }
            
            // Export reminders
            val reminders = db.reminderDao().getAllReminders()
            stringBuilder.append("REMINDERS (${reminders.size} total)\n")
            stringBuilder.append("-".repeat(30) + "\n\n")
            
            reminders.forEach { reminder ->
                stringBuilder.append("Title: ${reminder.title}\n")
                stringBuilder.append("Message: ${reminder.message}\n")
                stringBuilder.append("Scheduled: ${formatTimestamp(reminder.scheduledTime)}\n")
                stringBuilder.append("Status: ${if (reminder.isCompleted) "Completed" else "Pending"}\n")
                stringBuilder.append("\n" + "-".repeat(30) + "\n\n")
            }
            
            // Export preferences
            stringBuilder.append("APP PREFERENCES\n")
            stringBuilder.append("-".repeat(30) + "\n")
            val preferences = collectUserPreferences()
            preferences.forEach { (key, value) ->
                stringBuilder.append("$key: $value\n")
            }
            
            stringBuilder.append("\n\nThis export contains all your personal data from SwiftNote.\n")
            stringBuilder.append("Please keep this file secure as it contains your private information.\n")
            
            // Write to file
            val fileName = "swiftnote_readable_export_${getDateString()}.txt"
            val file = File(context.cacheDir, fileName)
            file.writeText(stringBuilder.toString())
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            
            Result.success(uri)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun collectUserPreferences(): Map<String, String> {
        val preferences = mutableMapOf<String, String>()
        
        // Theme preferences
        val themePrefs = context.getSharedPreferences("theme_preferences", Context.MODE_PRIVATE)
        preferences["theme_mode"] = themePrefs.getString("theme_mode", "system") ?: "system"
        
        // View mode preferences
        val viewPrefs = context.getSharedPreferences("view_mode_preferences", Context.MODE_PRIVATE)
        preferences["view_mode"] = viewPrefs.getString("view_mode", "grid") ?: "grid"
        
        // Consent preferences
        val consentPrefs = context.getSharedPreferences("privacy_consent_prefs", Context.MODE_PRIVATE)
        preferences["consent_version"] = consentPrefs.getInt("consent_version", 0).toString()
        preferences["privacy_policy_accepted"] = consentPrefs.getBoolean("privacy_policy_accepted", false).toString()
        preferences["notifications_consent"] = consentPrefs.getBoolean("notification_consent", false).toString()
        preferences["sync_consent"] = consentPrefs.getBoolean("sync_feature_consent", false).toString()
        
        return preferences
    }
    
    private fun formatTimestamp(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }
    
    private fun getCurrentTimestamp(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    private fun getDateString(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.versionCode})"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}