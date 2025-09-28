package com.amvarpvtltd.swiftNote

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.MutableLiveData
import com.amvarpvtltd.swiftNote.permissions.PermissionManager
import com.amvarpvtltd.swiftNote.permissions.createPermissionManager
import com.amvarpvtltd.swiftNote.ui.theme.SelfNoteTheme

class MainActivity : ComponentActivity() {

    // Modular permission manager
    private lateinit var permissionManager: PermissionManager

    // LiveData to hold the noteId from notification
    companion object {
        val noteIdToOpen = MutableLiveData<String?>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize modular permission manager
        initializePermissionManager()

        // Check for noteId in intent
        handleIntent(intent)

        setContent {
            SelfNoteTheme {
                Scaffold(modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Green) { _ ->
                    MyApp()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        // Check if we have a noteId in the intent (from notification)
        val noteId = intent.getStringExtra("noteId")
        if (!noteId.isNullOrEmpty()) {
            Log.d("MainActivity", "📱 Received noteId from notification: $noteId")
            // Set the LiveData value to trigger navigation in MyApp
            noteIdToOpen.value = noteId
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
