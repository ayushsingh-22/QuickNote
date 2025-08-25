package com.amvarpvtltd.selfnote.ai

import android.content.Context
import android.util.Log
import com.google.mlkit.nl.entityextraction.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume

/**
 * Smart Reminder AI using ML Kit Entity Extraction
 * Detects date/time information from note text using Google's on-device AI
 */
class SmartReminderAI(private val context: Context) {
    private val TAG = "SmartReminderAI"

    private lateinit var entityExtractor: EntityExtractor
    private var isInitialized = false

    companion object {
        @Volatile
        private var INSTANCE: SmartReminderAI? = null

        fun getInstance(context: Context): SmartReminderAI {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SmartReminderAI(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Initialize ML Kit Entity Extractor
     */
    suspend fun initialize(): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            suspendCancellableCoroutine<Result<String>> { continuation ->
                val options = EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH)
                    .build()

                entityExtractor = EntityExtraction.getClient(options)

                entityExtractor.downloadModelIfNeeded()
                    .addOnSuccessListener {
                        Log.d(TAG, "✅ ML Kit Entity Extractor initialized successfully")
                        isInitialized = true
                        continuation.resume(Result.success("AI initialized successfully"))
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "❌ Failed to initialize ML Kit Entity Extractor", exception)
                        continuation.resume(Result.failure(exception))
                    }

                continuation.invokeOnCancellation {
                    Log.d(TAG, "🔄 Entity Extractor initialization cancelled")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error initializing Entity Extractor", e)
            Result.failure(e)
        }
    }

    /**
     * Analyze text for date/time entities and return detected reminders
     */
    suspend fun analyzeTextForReminders(
        text: String,
        noteTitle: String = "Untitled"
    ): Result<List<DetectedReminder>> = withContext(Dispatchers.IO) {
        if (!isInitialized) {
            Log.w(TAG, "⚠️ Entity Extractor not initialized, attempting to initialize...")
            val initResult = initialize()
            if (initResult.isFailure) {
                return@withContext Result.failure(initResult.exceptionOrNull() ?: Exception("Failed to initialize"))
            }
        }

        return@withContext try {
            val combinedText = "$noteTitle. $text".trim()
            Log.d(TAG, "🔍 Analyzing text for reminders: ${combinedText.take(100)}...")

            suspendCancellableCoroutine<Result<List<DetectedReminder>>> { continuation ->
                entityExtractor.annotate(combinedText)
                    .addOnSuccessListener { entityAnnotations ->
                        val detectedReminders = processEntityAnnotations(
                            entityAnnotations,
                            combinedText,
                            noteTitle
                        )

                        Log.d(TAG, "✅ Found ${detectedReminders.size} potential reminders")
                        continuation.resume(Result.success(detectedReminders))
                    }
                    .addOnFailureListener { exception ->
                        Log.e(TAG, "❌ Error analyzing text for reminders", exception)
                        continuation.resume(Result.failure(exception))
                    }

                continuation.invokeOnCancellation {
                    Log.d(TAG, "🔄 Text analysis cancelled")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in analyzeTextForReminders", e)
            Result.failure(e)
        }
    }

    /**
     * Process ML Kit entity annotations to extract date/time information
     */
    private fun processEntityAnnotations(
        annotations: List<EntityAnnotation>,
        originalText: String,
        noteTitle: String
    ): List<DetectedReminder> {
        val reminders = mutableListOf<DetectedReminder>()
        val currentTime = System.currentTimeMillis()

        annotations.forEach { annotation ->
            annotation.entities.forEach { entity ->
                when (entity) {
                    is DateTimeEntity -> {
                        val reminderDateTime = processDateTimeEntity(entity, currentTime)

                        if (reminderDateTime != null && reminderDateTime > currentTime) {
                            val extractedText = originalText.substring(
                                annotation.start,
                                annotation.end.coerceAtMost(originalText.length)
                            )

                            val reminder = DetectedReminder(
                                id = UUID.randomUUID().toString(),
                                title = generateReminderTitle(extractedText, noteTitle),
                                description = generateReminderDescription(extractedText, originalText),
                                extractedText = extractedText,
                                reminderDateTime = reminderDateTime,
                                confidence = calculateConfidence(entity, extractedText),
                                entityType = "DateTime",
                                originalNoteTitle = noteTitle
                            )

                            reminders.add(reminder)
                            Log.d(TAG, "📅 Detected reminder: ${reminder.title} at ${formatDateTime(reminderDateTime)}")
                        }
                    }
                }
            }
        }

        return reminders.distinctBy { it.reminderDateTime }.sortedBy { it.reminderDateTime }
    }

    /**
     * Process DateTimeEntity to extract timestamp
     */
    private fun processDateTimeEntity(entity: DateTimeEntity, currentTime: Long): Long? {
        return try {
            val calendar = Calendar.getInstance()

            when (entity.dateTimeGranularity) {
                DateTimeEntity.GRANULARITY_DAY -> {
                    entity.timestampMillis?.let { timestamp ->
                        calendar.timeInMillis = timestamp
                        // Set to 9 AM if only date is specified
                        calendar.set(Calendar.HOUR_OF_DAY, 9)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.timeInMillis
                    }
                }
                DateTimeEntity.GRANULARITY_HOUR -> {
                    entity.timestampMillis
                }
                DateTimeEntity.GRANULARITY_MINUTE -> {
                    entity.timestampMillis
                }
                else -> {
                    entity.timestampMillis
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error processing DateTimeEntity", e)
            null
        }
    }

    /**
     * Generate a meaningful reminder title
     */
    private fun generateReminderTitle(extractedText: String, noteTitle: String): String {
        return when {
            extractedText.contains("appointment", ignoreCase = true) -> "📅 Appointment Reminder"
            extractedText.contains("meeting", ignoreCase = true) -> "🤝 Meeting Reminder"
            extractedText.contains("call", ignoreCase = true) -> "📞 Call Reminder"
            extractedText.contains("deadline", ignoreCase = true) -> "⏰ Deadline Reminder"
            extractedText.contains("reminder", ignoreCase = true) -> "🔔 Personal Reminder"
            noteTitle.isNotBlank() && noteTitle != "Untitled" -> "📝 $noteTitle"
            else -> "🔔 Smart Reminder"
        }
    }

    /**
     * Generate reminder description from context
     */
    private fun generateReminderDescription(extractedText: String, fullText: String): String {
        // Find the sentence containing the extracted text
        val sentences = fullText.split(Regex("[.!?]+"))
        val relevantSentence = sentences.find { it.contains(extractedText, ignoreCase = true) }

        return relevantSentence?.trim()?.takeIf { it.isNotBlank() }
            ?: extractedText.trim().takeIf { it.isNotBlank() }
            ?: "Reminder from your note"
    }

    /**
     * Format date/time for display
     */
    private fun formatDateTime(timestamp: Long): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    /**
     * Check if text contains potential reminder keywords
     */
    fun hasReminderKeywords(text: String): Boolean {
        val keywords = listOf(
            "tomorrow", "today", "tonight", "morning", "afternoon", "evening",
            "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday",
            "appointment", "meeting", "call", "deadline", "remind", "alarm",
            "at", "on", "by", "before", "after", "next", "this",
            "am", "pm", "o'clock", "oclock"
        )

        val lowercaseText = text.lowercase()
        return keywords.any { lowercaseText.contains(it) }
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        try {
            if (isInitialized) {
                entityExtractor.close()
                isInitialized = false
                Log.d(TAG, "🧹 Entity Extractor cleaned up")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error cleaning up Entity Extractor", e)
        }
    }

    /**
     * Calculate confidence for the detected reminder based on entity characteristics
     */
    private fun calculateConfidence(entity: DateTimeEntity, extractedText: String): Float {
        // Since ML Kit Entity doesn't provide confidence directly,
        // we calculate it based on various factors
        var confidence = 0.7f // Base confidence

        // Higher confidence for more specific time granularity
        when (entity.dateTimeGranularity) {
            DateTimeEntity.GRANULARITY_MINUTE -> confidence += 0.2f
            DateTimeEntity.GRANULARITY_HOUR -> confidence += 0.15f
            DateTimeEntity.GRANULARITY_DAY -> confidence += 0.1f
            else -> confidence += 0.05f
        }

        // Higher confidence for explicit reminder keywords
        val reminderKeywords = listOf("remind", "appointment", "meeting", "deadline", "alarm")
        if (reminderKeywords.any { extractedText.contains(it, ignoreCase = true) }) {
            confidence += 0.1f
        }

        // Ensure confidence is between 0.0 and 1.0
        return confidence.coerceIn(0.0f, 1.0f)
    }
}

/**
 * Data class representing a detected reminder
 */
data class DetectedReminder(
    val id: String,
    val title: String,
    val description: String,
    val extractedText: String,
    val reminderDateTime: Long,
    val confidence: Float,
    val entityType: String,
    val originalNoteTitle: String,
    val isConfirmed: Boolean = false
)
