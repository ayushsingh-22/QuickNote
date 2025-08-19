package com.amvarpvtltd.selfnote.utils

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.amvarpvtltd.selfnote.design.NoteTheme

object UIUtils {

    /**
     * Get color based on character count for progress indicators
     */
    @Composable
    fun getProgressColor(currentLength: Int, minLength: Int = 5, warningLength: Int = 3): Color {
        return when {
            currentLength >= minLength -> NoteTheme.Success
            currentLength >= warningLength -> NoteTheme.Warning
            else -> NoteTheme.Error
        }
    }

    /**
     * Calculate progress for circular indicators
     */
    fun calculateProgress(currentLength: Int, maxLength: Int): Float {
        return (currentLength.toFloat() / maxLength.toFloat()).coerceAtMost(1f)
    }

    /**
     * Get spring animation spec for UI elements
     */
    fun getSpringAnimationSpec() = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy)

    /**
     * Get tween animation spec for color transitions
     */
    fun getColorAnimationSpec() = tween<Color>(300)

    /**
     * Format character count display
     */
    fun formatCharacterCount(current: Int, max: Int): String {
        return "$current/$max"
    }

    /**
     * Get note card colors based on index
     */
    fun getNoteCardColors() = listOf(
        Color(0xFFF0F9FF) to Color(0xFF0EA5E9), // Blue
        Color(0xFFF0FDF4) to Color(0xFF10B981), // Green
        Color(0xFFFEF3C7) to Color(0xFFF59E0B), // Amber
        Color(0xFFFDF2F8) to Color(0xFFEC4899), // Pink
        Color(0xFFF3E8FF) to Color(0xFF8B5CF6), // Purple
        Color(0xFFECFDF5) to Color(0xFF059669), // Emerald
        Color(0xFFFFF1F2) to Color(0xFFE11D48), // Rose
        Color(0xFFF0F4FF) to Color(0xFF6366F1)  // Indigo
    )
}
