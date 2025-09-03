package com.amvarpvtltd.selfnote

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.amvarpvtltd.selfnote.design.AddScreen
import com.amvarpvtltd.selfnote.design.NotesScreen
import com.amvarpvtltd.selfnote.design.ViewNoteScreen
import com.amvarpvtltd.selfnote.design.NoteTheme
import com.amvarpvtltd.selfnote.offline.OfflineNoteManager
import com.amvarpvtltd.selfnote.theme.ProvideNoteTheme
import com.amvarpvtltd.selfnote.theme.rememberThemeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MyApp() {
    val navController = rememberNavController()

    // Initialize theme management
    val themeState = rememberThemeState()
    var currentTheme by themeState

    // Capture context once in composable scope
    val context = LocalContext.current

    // Initialize managers using captured context
    val offlineManager = remember(context) { OfflineNoteManager(context) }

    // State to track initialization
    var isInitializing by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf("onboarding") }

    // Initialize app and determine start destination
    LaunchedEffect(Unit) {
        try {
            Log.d("MyApp", "🚀 Starting app initialization...")

            // Check if we have a stored passphrase (new system)
            val storedPassphrase = com.amvarpvtltd.selfnote.auth.PassphraseManager.getStoredPassphrase(context)
            if (!storedPassphrase.isNullOrEmpty()) {
                myGlobalMobileDeviceId = storedPassphrase
                Log.d("MyApp", "✅ Found stored passphrase, going to main screen")
                startDestination = "main"
                isInitializing = false
                return@LaunchedEffect
            }

            // Fallback: check old device ID system for backward compatibility
            val storedId = com.amvarpvtltd.selfnote.auth.AuthManager.getStoredDeviceId(context)
            if (!storedId.isNullOrEmpty()) {
                myGlobalMobileDeviceId = storedId
                Log.d("MyApp", "✅ Found legacy device ID, going to main screen")
                startDestination = "main"
                isInitializing = false
                return@LaunchedEffect
            }

            // Check if we have local notes without a stored passphrase
            Log.d("MyApp", "📱 Checking local database...")
            val offlineNotes = withContext(Dispatchers.IO) { offlineManager.getAllNotes() }
            Log.d("MyApp", "📱 Found ${offlineNotes.size} notes in local database")

            if (offlineNotes.isNotEmpty()) {
                // Existing user without passphrase: migrate to new system
                Log.d("MyApp", "🔧 Migrating existing local data to new passphrase system")
                val newPassphrase = com.amvarpvtltd.selfnote.auth.PassphraseManager.generatePassphrase()
                val storeResult = com.amvarpvtltd.selfnote.auth.PassphraseManager.storePassphrase(context, newPassphrase)

                if (storeResult.isSuccess) {
                    myGlobalMobileDeviceId = newPassphrase
                    // Upload existing notes to Firebase
                    withContext(Dispatchers.IO) {
                        com.amvarpvtltd.selfnote.sync.SyncManager.uploadLocalDataToFirebase(context, newPassphrase)
                    }
                    startDestination = "main"
                } else {
                    Log.w("MyApp", "Failed to migrate to passphrase system, showing onboarding")
                    startDestination = "onboarding"
                }
                isInitializing = false
                return@LaunchedEffect
            }

            // No local data and no stored credentials: show onboarding
            Log.d("MyApp", "🆕 New user, showing onboarding")
            startDestination = "onboarding"

        } catch (e: Exception) {
            Log.e("MyApp", "❌ Error during app initialization", e)
            startDestination = "onboarding"
        } finally {
            isInitializing = false
        }
    }

    // Apply theme to entire app
    ProvideNoteTheme(themeMode = currentTheme) {
        Surface(color = MaterialTheme.colorScheme.surfaceTint) {
            if (isInitializing) {
                LoadingScreen()
            } else {
                NavigationComponent(navController, startDestination)
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = NoteTheme.Primary,
                strokeWidth = 4.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading QuickNote...",
                style = MaterialTheme.typography.titleMedium,
                color = NoteTheme.OnSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun NavigationComponent(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {
        // Onboarding & Auth
        composable("onboarding") {
            com.amvarpvtltd.selfnote.design.OnboardingScreen(navController)
        }

        // Main notes list
        composable("main") {
            NotesScreen(navController)
        }

        // Note Management
        composable("addscreen") {
            AddScreen(navController, noteId = null)
        }

        composable("addscreen/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            AddScreen(navController = navController, noteId = noteId)
        }

        composable("viewnote/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            ViewNoteScreen(navController = navController, noteId = noteId)
        }

        // Sync & Settings
        composable("syncSettings") {
            com.amvarpvtltd.selfnote.design.SyncSettingsScreen(navController)
        }

        composable("offlineSyncScreen") {
            com.amvarpvtltd.selfnote.design.OfflineSyncScreen(navController)
        }
    }
}
