package com.amvarpvtltd.selfnote.design

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

object NoteTheme {
    // Primary colors - using var to allow reassignment from ThemeManager
    var Primary by mutableStateOf(Color(0xFF6750A4))
    var PrimaryVariant by mutableStateOf(Color(0xFF4F46E5))
    var OnPrimary by mutableStateOf(Color(0xFFFFFFFF))
    var PrimaryContainer by mutableStateOf(Color(0xFFEADDFF))
    var OnPrimaryContainer by mutableStateOf(Color(0xFF21005D))

    // Secondary colors
    var Secondary by mutableStateOf(Color(0xFF625B71))
    var SecondaryVariant by mutableStateOf(Color(0xFF0891B2))
    var OnSecondary by mutableStateOf(Color(0xFFFFFFFF))
    var SecondaryContainer by mutableStateOf(Color(0xFFE8DEF8))
    var OnSecondaryContainer by mutableStateOf(Color(0xFF1D192B))

    // Surface colors
    var Surface by mutableStateOf(Color(0xFFFEF7FF))
    var OnSurface by mutableStateOf(Color(0xFF1D1B20))
    var SurfaceVariant by mutableStateOf(Color(0xFFE7E0EC))
    var OnSurfaceVariant by mutableStateOf(Color(0xFF49454F))

    // Background colors
    var Background by mutableStateOf(Color(0xFFFEF7FF))
    var OnBackground by mutableStateOf(Color(0xFF1D1B20))

    // Error colors
    var Error by mutableStateOf(Color(0xFFBA1A1A))
    var OnError by mutableStateOf(Color(0xFFFFFFFF))
    var ErrorContainer by mutableStateOf(Color(0xFFFFDAD6))
    var OnErrorContainer by mutableStateOf(Color(0xFF410002))

    // Warning colors
    var Warning by mutableStateOf(Color(0xFFFF9800))
    var OnWarning by mutableStateOf(Color(0xFFFFFFFF))
    var WarningContainer by mutableStateOf(Color(0xFFFFE0B2))
    var OnWarningContainer by mutableStateOf(Color(0xFF3E2723))

    // Success colors
    var Success by mutableStateOf(Color(0xFF4CAF50))
    var OnSuccess by mutableStateOf(Color(0xFFFFFFFF))
    var SuccessContainer by mutableStateOf(Color(0xFFC8E6C9))
    var OnSuccessContainer by mutableStateOf(Color(0xFF1B5E20))

    // Outline colors
    var Outline by mutableStateOf(Color(0xFF79747E))
    var OutlineVariant by mutableStateOf(Color(0xFFCAC4D0))

    // Dark theme colors
    object Dark {
        var Primary by mutableStateOf(Color(0xFFD0BCFF))
        var OnPrimary by mutableStateOf(Color(0xFF381E72))
        var PrimaryContainer by mutableStateOf(Color(0xFF4F378B))
        var OnPrimaryContainer by mutableStateOf(Color(0xFFEADDFF))

        var Secondary by mutableStateOf(Color(0xFFCCC2DC))
        var OnSecondary by mutableStateOf(Color(0xFF332D41))
        var SecondaryContainer by mutableStateOf(Color(0xFF4A4458))
        var OnSecondaryContainer by mutableStateOf(Color(0xFFE8DEF8))

        var Surface by mutableStateOf(Color(0xFF141218))
        var OnSurface by mutableStateOf(Color(0xFFE6E0E9))
        var SurfaceVariant by mutableStateOf(Color(0xFF49454F))
        var OnSurfaceVariant by mutableStateOf(Color(0xFFCAC4D0))

        var Background by mutableStateOf(Color(0xFF141218))
        var OnBackground by mutableStateOf(Color(0xFFE6E0E9))

        var Error by mutableStateOf(Color(0xFFFFB4AB))
        var OnError by mutableStateOf(Color(0xFF690005))
        var ErrorContainer by mutableStateOf(Color(0xFF93000A))
        var OnErrorContainer by mutableStateOf(Color(0xFFFFDAD6))

        var Warning by mutableStateOf(Color(0xFFFFCC02))
        var OnWarning by mutableStateOf(Color(0xFF3E2723))
        var WarningContainer by mutableStateOf(Color(0xFFFF8F00))
        var OnWarningContainer by mutableStateOf(Color(0xFFFFE0B2))

        var Success by mutableStateOf(Color(0xFF81C784))
        var OnSuccess by mutableStateOf(Color(0xFF1B5E20))
        var SuccessContainer by mutableStateOf(Color(0xFF2E7D32))
        var OnSuccessContainer by mutableStateOf(Color(0xFFC8E6C9))

        var Outline by mutableStateOf(Color(0xFF938F99))
        var OutlineVariant by mutableStateOf(Color(0xFF49454F))
    }
}
