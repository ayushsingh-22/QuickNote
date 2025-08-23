package com.amvarpvtltd.selfnote.utils

object ValidationUtils {

    /**
     * Check if title is valid (meets minimum requirements)
     */
    fun isValidTitle(title: String): Boolean {
        return title.trim().length >= Constants.MIN_CONTENT_LENGTH
    }

    /**
     * Check if description is valid (optional but if provided, should meet minimum)
     */
    fun isValidDescription(description: String): Boolean {
        return description.trim().isEmpty() || description.trim().length >= Constants.MIN_CONTENT_LENGTH
    }

    /**
     * Check if a note can be saved (title is valid)
     */
    fun canSaveNote(title: String, description: String): Boolean {
        return isValidTitle(title)
    }

    /**
     * Get validation message for title
     */
    fun getTitleValidationMessage(): String {
        return "Title must be at least ${Constants.MIN_CONTENT_LENGTH} characters"
    }

    /**
     * Get validation message for save requirements
     */
    fun getSaveValidationMessage(): String {
        return "Please enter a title with at least ${Constants.MIN_CONTENT_LENGTH} characters to save your note"
    }

    /**
     * Check if content length is approaching limit
     */
    fun isApproachingLimit(currentLength: Int, maxLength: Int): Boolean {
        return currentLength > (maxLength * 0.8) // 80% of max length
    }

    /**
     * Check if content length exceeds limit
     */
    fun exceedsLimit(currentLength: Int, maxLength: Int): Boolean {
        return currentLength >= maxLength
    }
}
