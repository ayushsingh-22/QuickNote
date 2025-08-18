package com.amvarpvtltd.selfnote.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

object ColorResources {
    @Composable
    fun getColor(colorResId: Int): Color {
        val context = LocalContext.current
        return Color(context.getColor(colorResId))
    }
}