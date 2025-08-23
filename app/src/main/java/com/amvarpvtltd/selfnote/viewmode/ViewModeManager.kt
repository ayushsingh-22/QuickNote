package com.amvarpvtltd.selfnote.viewmode

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material.icons.outlined.ViewModule
import androidx.compose.ui.graphics.vector.ImageVector
import com.amvarpvtltd.selfnote.utils.Constants

enum class ViewMode {
    CARD, LIST, GRID
}

object ViewModeManager {
    fun getViewMode(context: Context): ViewMode {
        val prefs = context.getSharedPreferences(Constants.VIEW_MODE_PREFERENCES, Context.MODE_PRIVATE)
        val viewModeName = prefs.getString(Constants.VIEW_MODE_KEY, Constants.DEFAULT_VIEW_MODE)
        return when (viewModeName) {
            Constants.VIEW_MODE_LIST -> ViewMode.LIST
            Constants.VIEW_MODE_GRID -> ViewMode.GRID
            else -> ViewMode.CARD
        }
    }

    fun setViewMode(context: Context, viewMode: ViewMode) {
        val prefs = context.getSharedPreferences(Constants.VIEW_MODE_PREFERENCES, Context.MODE_PRIVATE)
        val viewModeName = when (viewMode) {
            ViewMode.LIST -> Constants.VIEW_MODE_LIST
            ViewMode.GRID -> Constants.VIEW_MODE_GRID
            ViewMode.CARD -> Constants.VIEW_MODE_CARD
        }
        prefs.edit().putString(Constants.VIEW_MODE_KEY, viewModeName).apply()
    }

    fun getViewModeIcon(viewMode: ViewMode): ImageVector {
        return when (viewMode) {
            ViewMode.CARD -> Icons.Outlined.ViewModule
            ViewMode.LIST -> Icons.Outlined.ViewList
            ViewMode.GRID -> Icons.Outlined.GridView
        }
    }

    fun getViewModeLabel(viewMode: ViewMode): String {
        return when (viewMode) {
            ViewMode.CARD -> "Card View"
            ViewMode.LIST -> "List View"
            ViewMode.GRID -> "Grid View"
        }
    }
}
