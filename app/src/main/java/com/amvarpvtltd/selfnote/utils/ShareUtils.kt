package com.amvarpvtltd.selfnote.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import dataclass

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
     * Share multiple notes to other apps
     */
    fun shareMultipleNotes(context: Context, notes: List<dataclass>) {
        try {
            val shareText = formatMultipleNotesForSharing(notes)
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = Constants.SHARE_MIME_TYPE
                putExtra(Intent.EXTRA_SUBJECT, "Notes from QuickNote (${notes.size} notes)")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share ${notes.size} notes via...")
            chooserIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooserIntent)

        } catch (e: Exception) {
            Toast.makeText(context, "❌ Error sharing notes", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Copy note content to clipboard
     */
    fun copyNoteToClipboard(context: Context, note: dataclass) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Note", formatNoteForSharing(note))
            clipboard.setPrimaryClip(clip)

            Toast.makeText(context, "📋 Note copied to clipboard", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "❌ Error copying note", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Format a single note for sharing
     */
    private fun formatNoteForSharing(note: dataclass): String {
        return buildString {
            appendLine("📝 ${note.title}")
            appendLine("=".repeat(note.title.length))
            appendLine()
            if (note.description.isNotEmpty()) {
                appendLine(note.description)
                appendLine()
            }
            appendLine("--")

        }
    }

    /**
     * Format multiple notes for sharing
     */
    private fun formatMultipleNotesForSharing(notes: List<dataclass>): String {
        return buildString {
            appendLine("📚 My Notes Collection (${notes.size} notes)")
            appendLine("=".repeat(40))
            appendLine()

            notes.forEachIndexed { index, note ->
                appendLine("${index + 1}. 📝 ${note.title}")
                appendLine("-".repeat(note.title.length + 6))
                if (note.description.isNotEmpty()) {
                    appendLine(note.description)
                }
                appendLine()
            }

            appendLine("--")

        }
    }
}
