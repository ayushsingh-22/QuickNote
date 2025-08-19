package com.amvarpvtltd.selfnote.utils

object Constants {
    // Character limits
    const val TITLE_MAX_LENGTH = 50
    const val DESCRIPTION_MAX_LENGTH = 1000
    const val MIN_CONTENT_LENGTH = 5
    const val WARNING_LENGTH = 3

    // Animation durations
    const val COLOR_ANIMATION_DURATION = 300
    const val SPRING_ANIMATION_DELAY = 150
    const val LOADING_DELAY = 300L
    const val REFRESH_DELAY = 500L

    // UI Messages
    const val SAVE_SUCCESS_MESSAGE = "✅ Note saved successfully!"
    const val UPDATE_SUCCESS_MESSAGE = "✅ Note updated successfully!"
    const val DELETE_SUCCESS_MESSAGE = "🗑️ Note deleted successfully"
    const val ERROR_LOADING_MESSAGE = "❌ Error loading note"
    const val ERROR_SAVING_MESSAGE = "❌ Error saving note"
    const val ERROR_DELETING_MESSAGE = "❌ Error deleting note"
    const val VALIDATION_WARNING_MESSAGE = "⚠️ Title or description must be at least 5 characters"

    // UI Sizes
    const val FAB_SIZE = 64
    const val ICON_SIZE_SMALL = 16
    const val ICON_SIZE_MEDIUM = 20
    const val ICON_SIZE_LARGE = 24
    const val PROGRESS_INDICATOR_SIZE = 24

    // Padding and spacing
    const val PADDING_SMALL = 8
    const val PADDING_MEDIUM = 16
    const val PADDING_LARGE = 24
    const val PADDING_XL = 32

    // Corner radius
    const val CORNER_RADIUS_SMALL = 12
    const val CORNER_RADIUS_MEDIUM = 16
    const val CORNER_RADIUS_LARGE = 20
    const val CORNER_RADIUS_XL = 24
}
