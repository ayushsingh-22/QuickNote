package com.amvarpvtltd.selfnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.amvarpvtltd.selfnote.permissions.PermissionManager
import com.amvarpvtltd.selfnote.permissions.createPermissionManager
import com.amvarpvtltd.selfnote.ui.theme.SelfNoteTheme

class MainActivity : ComponentActivity() {

    // Modular permission manager
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize modular permission manager
        initializePermissionManager()

        setContent {
            SelfNoteTheme {
                Scaffold(modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Green) { _ ->
                    MyApp()
                }
            }
        }
    }

    private fun initializePermissionManager() {
        // Create permission manager with custom callback
        permissionManager = createPermissionManager { isGranted ->
            // Custom handling when permission result is received
            if (isGranted) {
                // Permission granted - you can add any additional logic here
                // For example: initialize notification channels, update UI, etc.
                onNotificationPermissionGranted()
            } else {
                // Permission denied - handle accordingly
                onNotificationPermissionDenied()
            }
        }

        // Request notification permission on app startup
        permissionManager.requestNotificationPermissionIfNeeded()
    }

    private fun onNotificationPermissionGranted() {
        // Additional logic when notification permission is granted
        // This is where you can add future enhancements like:
        // - Initialize notification channels
        // - Enable reminder features
        // - Update app settings
    }

    private fun onNotificationPermissionDenied() {
        // Additional logic when notification permission is denied
        // This is where you can add future enhancements like:
        // - Show alternative reminder options
        // - Disable notification-dependent features
        // - Guide user to settings
    }
}
