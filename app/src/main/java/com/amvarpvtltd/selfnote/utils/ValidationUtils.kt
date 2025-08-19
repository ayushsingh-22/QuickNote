package com.amvarpvtltd.selfnote.utils

object ValidationUtils {

    /**
     * Check if a note can be saved based on title requirements only
     * Title must be at least 5 characters, description has no minimum requirement
     */
    fun canSaveNote(title: String, description: String): Boolean {
        return title.trim().length >= 5
    }

    /**
     * Validate title length - must be at least 5 characters
     */
    fun isValidTitle(title: String): Boolean {
        return title.trim().length >= 5
    }

    /**
     * Validate description - no minimum requirement, always valid
     */
    fun isValidDescription(description: String): Boolean {
        return true // No minimum requirement for description
    }

    /**
     * Get validation message for save requirements
     */
    fun getSaveValidationMessage(): String {
        return "Title must be at least 5 characters to save"
    }

    /**
     * Get validation message for title
     */
    fun getTitleValidationMessage(): String {
        return "Title must be at least 5 characters"
    }

    /**
     * Get validation message for description
     */
    fun getDescriptionValidationMessage(): String {
        return "" // No validation message needed since no minimum requirement
    }
}
