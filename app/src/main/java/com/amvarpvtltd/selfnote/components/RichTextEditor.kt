package com.amvarpvtltd.selfnote.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.amvarpvtltd.selfnote.design.NoteTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RichTextEditor(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isPreview: Boolean = false
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(text)) }
    val hapticFeedback = LocalHapticFeedback.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Format button data class
    data class FormatButton(
        val icon: ImageVector,
        val description: String,
        val prefix: String,
        val suffix: String,
        val linePrefix: String = "",
        val isListType: Boolean = false,
        val isNumbered: Boolean = false,
        val isCheckbox: Boolean = false
    )

    // Define format buttons
    val formatButtons = listOf(
        FormatButton(
            icon = Icons.Outlined.FormatBold,
            description = "Bold",
            prefix = "**",
            suffix = "**"
        ),
        FormatButton(
            icon = Icons.Outlined.FormatItalic,
            description = "Italic",
            prefix = "_",
            suffix = "_"
        ),
        FormatButton(
            icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
            description = "Bullet List",
            prefix = "",
            suffix = "",
            linePrefix = "* ",
            isListType = true
        ),
        FormatButton(
            icon = Icons.Outlined.FormatListNumbered,
            description = "Numbered List",
            prefix = "",
            suffix = "",
            linePrefix = "1. ",
            isListType = true,
            isNumbered = true
        ),
        FormatButton(
            icon = Icons.Outlined.CheckBox,
            description = "Checkbox",
            prefix = "",
            suffix = "",
            linePrefix = "- [ ] ",
            isListType = true,
            isCheckbox = true
        )
    )

    fun handleNewLine(currentText: String, cursorPosition: Int): String {
        val lines = currentText.lines()
        val currentLineIndex = currentText.take(cursorPosition).count { it == '\n' }
        val currentLine = lines.getOrNull(currentLineIndex) ?: return currentText

        // Enhanced list detection regex to handle all list types
        val listMatch = Regex("^(\\s*)((?:\\d+\\.|\\*|-\\s\\[[ x]\\])\\s)(.*)$").find(currentLine)

        return if (listMatch != null) {
            val (indent, listMarker, content) = listMatch.destructured

            // If the current line is empty (except for the marker), remove the formatting
            if (content.isEmpty()) {
                val beforeCursor = currentText.substring(0, cursorPosition - listMarker.length)
                val afterCursor = currentText.substring(cursorPosition)
                beforeCursor + "\n" + afterCursor
            } else {
                // Determine the next list marker based on the current type
                val newMarker = when {
                    // Keep the same checkbox state for new checkbox items
                    listMarker.startsWith("- [") -> "- [ ] "
                    // Keep the same bullet for bullet lists
                    listMarker.startsWith("*") -> "* "
                    // Auto-increment numbered lists by finding the current number and adding 1
                    listMarker.contains(Regex("\\d+\\.")) -> {
                        val currentNum = listMarker.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
                        "${currentNum + 1}. "
                    }
                    else -> listMarker
                }

                val beforeCursor = currentText.substring(0, cursorPosition)
                val afterCursor = currentText.substring(cursorPosition)
                "$beforeCursor\n$indent$newMarker$afterCursor"
            }
        } else {
            // No list formatting, just add normal newline
            val beforeCursor = currentText.substring(0, cursorPosition)
            val afterCursor = currentText.substring(cursorPosition)
            "$beforeCursor\n$afterCursor"
        }
    }

    fun handleBackspace(currentText: String, cursorPosition: Int): String {
        val lines = currentText.lines()
        val currentLineIndex = currentText.take(cursorPosition).count { it == '\n' }
        val currentLine = lines.getOrNull(currentLineIndex) ?: return currentText

        // Enhanced behavior: Only remove list formatting when at start of line and previous key was backspace
        if (cursorPosition > 0 && currentText[cursorPosition - 1] == '\n') {
            val listMatch = Regex("^(\\s*)((?:\\d+\\.|\\*|-\\s\\[[ x]\\])\\s)(.*)$").find(currentLine)

            if (listMatch != null) {
                val (indent, listMarker, content) = listMatch.destructured
                if (content.isEmpty()) {
                    // Remove the list formatting while preserving the cursor position
                    val lineStart = currentText.lastIndexOf('\n', cursorPosition - 2).let {
                        if (it == -1) 0 else it + 1
                    }
                    val beforeLine = currentText.substring(0, lineStart)
                    val afterLine = currentText.substring(cursorPosition)
                    return beforeLine + afterLine
                }
            }
        }
        return currentText
    }

    fun toggleCheckbox(currentText: String, cursorPosition: Int): String {
        val lines = currentText.lines()
        val currentLineIndex = currentText.take(cursorPosition).count { it == '\n' }
        val currentLine = lines.getOrNull(currentLineIndex) ?: return currentText

        // Enhanced checkbox detection with support for both spaces and 'x'
        val checkboxMatch = Regex("^(\\s*-\\s\\[)([\\sx])(\\]\\s.*)$").find(currentLine)
        if (checkboxMatch != null) {
            val (prefix, state, suffix) = checkboxMatch.destructured
            // Toggle between unchecked " " and checked "x"
            val newState = if (state == " ") "x" else " "

            // Calculate correct line start position for multi-line text
            val lineStart = if (currentLineIndex > 0) {
                lines.take(currentLineIndex).joinToString("\n").length + 1
            } else 0

            val beforeLine = currentText.substring(0, lineStart)
            val afterLine = currentText.substring(lineStart + currentLine.length)
            val newLine = prefix + newState + suffix

            return beforeLine + newLine + afterLine
        }
        return currentText
    }

    fun applyFormat(button: FormatButton) {
        val currentText = textFieldValue.text
        val selection = textFieldValue.selection
        val start = selection.start.coerceIn(0, currentText.length)
        val end = selection.end.coerceIn(0, currentText.length)

        if (button.isListType) {
            // Handle list formatting
            val lines = currentText.lines().toMutableList()
            val startLine = currentText.take(start).count { it == '\n' }
            val endLine = currentText.take(end).count { it == '\n' }

            // Check if current lines are already formatted with this specific prefix
            val isAlreadyFormatted = lines.slice(startLine..endLine.coerceAtMost(lines.lastIndex))
                .all { it.trimStart().startsWith(button.linePrefix.trim()) }

            // Apply or remove formatting
            for (i in startLine..endLine.coerceAtMost(lines.lastIndex)) {
                val line = lines[i]
                lines[i] = if (isAlreadyFormatted) {
                    // Remove formatting
                    line.replaceFirst(Regex("^\\s*(\\d+\\.|\\*|-\\s\\[[ x]\\])\\s"), "")
                } else {
                    // Apply new formatting
                    if (line.isNotEmpty()) {
                        // Remove any existing list formatting first
                        val cleanLine = line.replaceFirst(Regex("^\\s*(\\d+\\.|\\*|-\\s\\[[ x]\\])\\s"), "")
                        val newPrefix = if (button.isNumbered) "${i - startLine + 1}. " else button.linePrefix
                        newPrefix + cleanLine
                    } else line
                }
            }

            // Update text while preserving cursor position
            val newText = lines.joinToString("\n")
            val newCursorPos = if (end > start) {
                val newStart = calculateNewPosition(currentText, newText, start)
                val newEnd = calculateNewPosition(currentText, newText, end)
                TextRange(newStart, newEnd)
            } else {
                val lineEnd = lines[startLine].length
                val totalOffset = lines.take(startLine).sumOf { it.length + 1 } + lineEnd
                TextRange(totalOffset, totalOffset)
            }

            textFieldValue = TextFieldValue(newText, newCursorPos)
            onTextChange(newText)
        } else {
            // Handle inline formatting
            val selectedText = if (start < end) currentText.substring(start, end) else ""

            val newText = if (selectedText.isEmpty()) {
                val beforeCursor = currentText.substring(0, start)
                val afterCursor = currentText.substring(end)
                "$beforeCursor${button.prefix}${button.suffix}$afterCursor"
            } else {
                val beforeSelection = currentText.substring(0, start)
                val afterSelection = currentText.substring(end)
                "$beforeSelection${button.prefix}$selectedText${button.suffix}$afterSelection"
            }

            val newCursorPos = if (selectedText.isEmpty()) {
                val pos = start + button.prefix.length
                TextRange(pos, pos)
            } else {
                TextRange(
                    start + button.prefix.length,
                    end + button.prefix.length
                )
            }

            textFieldValue = TextFieldValue(newText, newCursorPos)
            onTextChange(newText)
        }

        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Column(modifier = modifier) {
        // Formatting toolbar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = NoteTheme.Surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                formatButtons.forEach { button ->
                    IconButton(
                        onClick = { applyFormat(button) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = button.icon,
                            contentDescription = button.description,
                            tint = NoteTheme.OnSurface
                        )
                    }

                    if (button.description == "Italic") {
                        // Add divider after text formatting buttons
                        HorizontalDivider(
                            modifier = Modifier
                                .height(24.dp)
                                .width(1.dp)
                                .background(NoteTheme.OnSurfaceVariant.copy(alpha = 0.3f))
                        )
                    }
                }
            }
        }

        // Text editor
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                // Handle key events
                if (newValue.text.length > textFieldValue.text.length &&
                    newValue.text[textFieldValue.text.length] == '\n') {
                    // Handle new line with list continuation
                    val newText = handleNewLine(textFieldValue.text, textFieldValue.selection.start)
                    textFieldValue = TextFieldValue(
                        newText,
                        TextRange(newText.length)
                    )
                } else if (newValue.text.length < textFieldValue.text.length &&
                    textFieldValue.selection.start > 0 &&
                    textFieldValue.text[textFieldValue.selection.start - 1] == '\n') {
                    // Handle backspace at start of list item
                    val newText = handleBackspace(textFieldValue.text, textFieldValue.selection.start)
                    textFieldValue = TextFieldValue(
                        newText,
                        TextRange(newValue.selection.start)
                    )
                } else {
                    textFieldValue = newValue
                }
                onTextChange(textFieldValue.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 150.dp)
                .focusRequester(focusRequester)
                .onKeyEvent { event ->
                    if (event.key == Key.Spacebar && event.type == KeyEventType.KeyDown) {
                        // Check if we're on a checkbox line and toggle it
                        val newText = toggleCheckbox(textFieldValue.text, textFieldValue.selection.start)
                        if (newText != textFieldValue.text) {
                            textFieldValue = TextFieldValue(newText, textFieldValue.selection)
                            onTextChange(newText)
                            return@onKeyEvent true
                        }
                    }
                    false
                },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NoteTheme.Primary,
                unfocusedBorderColor = NoteTheme.OnSurfaceVariant.copy(alpha = 0.3f),
                focusedTextColor = NoteTheme.OnSurface,
                unfocusedTextColor = NoteTheme.OnSurface,
                cursorColor = NoteTheme.Primary,
                focusedContainerColor = NoteTheme.Surface,
                unfocusedContainerColor = NoteTheme.Surface
            ),
            placeholder = {
                Text(
                    "Write your thoughts here...\n\nUse the toolbar above to format your text.",
                    color = NoteTheme.OnSurfaceVariant.copy(alpha = 0.5f)
                )
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Default
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            )
        )
    }
}

// Helper function to calculate new cursor position after formatting
private fun calculateNewPosition(oldText: String, newText: String, oldPos: Int): Int {
    val beforeCursor = oldText.take(oldPos)
    val commonPrefix = newText.commonPrefixWith(beforeCursor)
    return commonPrefix.length
}
