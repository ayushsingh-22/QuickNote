package com.amvarpvtltd.selfnote.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.amvarpvtltd.selfnote.dataclass

object ShareUtils {

    /**
     * Share a single note to other apps
     */
    fun shareNote(context: Context, note: dataclass) {
        try {
            val shareText = formatNoteForSharing(note)
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = Constants.SHARE_MIME_TYPE
                putExtra(Intent.EXTRA_SUBJECT, Constants.SHARE_SUBJECT)
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share note via...")
            chooserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooserIntent)

        } catch (e: Exception) {
            Toast.makeText(context, "❌ Error sharing note", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Copy note content to clipboard
     */
    fun copyNoteToClipboard(context: Context, note: dataclass) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val noteText = formatNoteForSharing(note)
            val clip = ClipData.newPlainText("QuickNote - ${note.title}", noteText)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(context, "📋 Note copied to clipboard", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "❌ Error copying note", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Format a single note for sharing with enhanced formatting
     */
    private fun formatNoteForSharing(note: dataclass): String {
        return buildString {
            // Title Section
            appendLine("📝 Note")
            appendLine("Title: ${note.title}")
            appendLine("=".repeat(maxOf(note.title.length + 8, 25)))
            appendLine()

            // Description Section
            if (note.description.isNotEmpty()) {
                appendLine("📌 Description:")
                appendLine(note.description.trim())
                appendLine()
            } else {
                appendLine("📌 Description: (No details provided)")
                appendLine()
            }

        }
    }


    /**
     * Format multiple notes for sharing with enhanced formatting
     */
    private fun formatMultipleNotesForSharing(notes: List<dataclass>): String {
        return buildString {
            appendLine("📚 My Notes Collection (${notes.size} notes)")
            appendLine("=".repeat(50))
            appendLine()

            notes.forEachIndexed { index, note ->
                appendLine("${index + 1}. 📝 ${note.title}")
                appendLine("-".repeat(note.title.length + 6))
                if (note.description.isNotEmpty()) {
                    // Limit description length for better readability when sharing multiple notes
                    val description = if (note.description.length > 200) {
                        "${note.description.take(200)}..."
                    } else {
                        note.description
                    }
                    appendLine(description)
                }
                appendLine()
            }

            appendLine("--")
            appendLine("📱 Shared from QuickNote")
        }
    }
}
