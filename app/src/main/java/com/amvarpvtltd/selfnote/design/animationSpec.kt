package com.amvarpvtltd.selfnote.design

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

object NoteTheme {
    var Primary by mutableStateOf(Color(0xFF6366F1))
    var PrimaryVariant by mutableStateOf(Color(0xFF4F46E5))
    var Secondary by mutableStateOf(Color(0xFF06B6D4))
    var SecondaryVariant by mutableStateOf(Color(0xFF0891B2))
    var Background by mutableStateOf(Color(0xFFFAFAFA))
    var Surface by mutableStateOf(Color(0xFFFFFFFF))
    var SurfaceVariant by mutableStateOf(Color(0xFFF1F5F9))
    var OnPrimary by mutableStateOf(Color(0xFFFFFFFF))
    var OnBackground by mutableStateOf(Color(0xFF1E293B))
    var OnSurface by mutableStateOf(Color(0xFF334155))
    var OnSurfaceVariant by mutableStateOf(Color(0xFF64748B))
    var Success by mutableStateOf(Color(0xFF10B981))
    var Warning by mutableStateOf(Color(0xFFF59E0B))
    var Error by mutableStateOf(Color(0xFFEF4444))
    var ErrorContainer by mutableStateOf(Color(0xFFFEE2E2))
}