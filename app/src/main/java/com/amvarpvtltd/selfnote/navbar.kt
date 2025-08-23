package com.amvarpvtltd.selfnote

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.amvarpvtltd.selfnote.design.AddScreen
import com.amvarpvtltd.selfnote.design.NotesScreen
import com.amvarpvtltd.selfnote.design.welcomeScreen
import com.amvarpvtltd.selfnote.design.ViewNoteScreen
import com.amvarpvtltd.selfnote.design.OfflineSyncScreen // Add this import
import com.amvarpvtltd.selfnote.design.OfflineScreen // Add this import
import com.amvarpvtltd.selfnote.theme.rememberThemeState
import com.amvarpvtltd.selfnote.offline.OfflineNoteManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import android.widget.Toast
import myGlobalMobileDeviceId

@Composable
fun MyApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Initialize theme management
    val themeState = rememberThemeState()
    var currentTheme by themeState

    // Initialize offline manager
    val offlineManager = OfflineNoteManager(context)

    myGlobalMobileDeviceId = generateUniqueDeviceId(context = context)

    LaunchedEffect(Unit) {
        try {
            // Check offline notes first for better performance
            val offlineNotes = offlineManager.offlineNotes.value

            if (offlineNotes.isNotEmpty()) {
                // Has offline notes, go to notes screen
                navController.navigate("noteScreen") {
                    popUpTo("welcome") { inclusive = true }
                }
            } else {
                // No offline notes, check online
                val hasOnlineData = withContext(Dispatchers.IO) {
                    try {
                        checkFirebaseData()
                    } catch (e: Exception) {
                        false
                    }
                }

                val startDestination = if (hasOnlineData) "noteScreen" else "welcome"
                navController.navigate(startDestination) {
                    popUpTo("welcome") { inclusive = true }
                }
            }
        } catch (e: Exception) {
            // Fallback to welcome screen
            navController.navigate("welcome") {
                popUpTo("welcome") { inclusive = true }
            }
        }
    }

    // Auto-sync when app starts if online
    LaunchedEffect(Unit) {
        try {
            if (offlineManager.hasPendingSync()) {
                // Show toast that sync will happen
                Toast.makeText(context, "🔄 Syncing your notes...", Toast.LENGTH_SHORT).show()

                withContext(Dispatchers.IO) {
                    try {
                        // Your sync logic here
                        // offlineManager.syncWithFirebase(...)
                    } catch (e: Exception) {
                        // Sync will happen later when online
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore sync errors on startup
        }
    }

    // Apply theme to entire app
    com.amvarpvtltd.selfnote.theme.ProvideNoteTheme(themeMode = currentTheme) {
        Surface(color = MaterialTheme.colorScheme.surfaceTint) {
            NavigationComponent(navController)
        }
    }
}

@Composable
fun NavigationComponent(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "welcome") {
        composable("addscreen") { AddScreen(navController, noteId = null) }

        composable("welcome") {
            welcomeScreen(navController)
        }

        composable("addscreen/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            AddScreen(navController = navController, noteId = noteId)
        }

        composable("viewnote/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            ViewNoteScreen(navController = navController, noteId = noteId)
        }

        composable("noteScreen") {
            NotesScreen(navController)
        }

        // Add the new offline sync screen route
        composable("offlineSyncScreen") {
            OfflineSyncScreen(navController)
        }

        // Add the new offline screen route
        composable("offlineScreen") {
            OfflineScreen(navController)
        }
    }
}

suspend fun checkFirebaseData(): Boolean {
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val notesRef: DatabaseReference = database.getReference("notes").child(myGlobalMobileDeviceId)

    return try {
        val snapshot = notesRef.limitToFirst(1).get().await()
        snapshot.exists()
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
