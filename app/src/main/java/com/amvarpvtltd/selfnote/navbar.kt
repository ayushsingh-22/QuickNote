package com.amvarpvtltd.selfnote

import android.util.Log
import android.widget.Toast
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
import com.amvarpvtltd.selfnote.design.welcomeScreen
import com.amvarpvtltd.selfnote.design.NoteTheme
import com.amvarpvtltd.selfnote.offline.OfflineNoteManager
import com.amvarpvtltd.selfnote.theme.ProvideNoteTheme
import com.amvarpvtltd.selfnote.theme.rememberThemeState
import com.amvarpvtltd.selfnote.utils.NetworkManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Composable
fun MyApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Initialize theme management
    val themeState = rememberThemeState()
    var currentTheme by themeState

    // Initialize managers
    val offlineManager = OfflineNoteManager(context)
    val networkManager = NetworkManager.getInstance(context)
    val noteRepository = remember { com.amvarpvtltd.selfnote.repository.NoteRepository(context) }

    // State to track initialization
    var isInitializing by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf("welcome") }

    myGlobalMobileDeviceId = generateUniqueDeviceId(context = context)

    LaunchedEffect(Unit) {
        try {
            Log.d("MyApp", "🚀 Starting app initialization...")

            // Step 1: Always check local Room database first (offline-first approach)
            Log.d("MyApp", "📱 Checking local database...")
            val offlineNotes = withContext(Dispatchers.IO) {
                offlineManager.getAllNotes()
            }
            Log.d("MyApp", "📱 Found ${offlineNotes.size} notes in local database")

            if (offlineNotes.isNotEmpty()) {
                // Has local notes - set notes screen as start destination
                Log.d("MyApp", "✅ Local notes found - setting notes screen as start destination")
                startDestination = "noteScreen"
                isInitializing = false
                return@LaunchedEffect
            }

            // Step 2: No local notes found - check if we're online to fetch from Firebase
            val isOnline = networkManager.isConnected()
            Log.d("MyApp", "🌐 Network status: ${if (isOnline) "ONLINE" else "OFFLINE"}")

            if (isOnline) {
                // Step 3: Check Firebase for existing data
                Log.d("MyApp", "☁️ Checking Firebase for existing notes...")
                val hasFirebaseData = withContext(Dispatchers.IO) {
                    try {
                        checkAndSyncFirebaseData(noteRepository)
                    } catch (e: Exception) {
                        Log.e("MyApp", "❌ Error checking Firebase data", e)
                        false
                    }
                }

                if (hasFirebaseData) {
                    // Step 4: Firebase data found and synced - set notes screen as start destination
                    Log.d("MyApp", "✅ Firebase data found and synced - setting notes screen as start destination")
                    startDestination = "noteScreen"
                } else {
                    // Step 5: No data anywhere - set welcome screen as start destination
                    Log.d("MyApp", "👋 No data found anywhere - setting welcome screen as start destination")
                    startDestination = "welcome"
                }
            } else {
                // Step 6: Offline with no local notes - set welcome screen as start destination
                Log.d("MyApp", "📵 Offline with no local notes - setting welcome screen as start destination")
                startDestination = "welcome"

                // Show offline message
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "📱 You're offline. Connect to internet to sync your notes or create new ones locally.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            Log.e("MyApp", "❌ Error during app initialization", e)
            // Fallback to welcome screen on any error
            startDestination = "welcome"
        } finally {
            isInitializing = false
        }
    }

    // Auto-sync when app starts if online and has pending changes
    LaunchedEffect(Unit) {
        try {
            if (offlineManager.hasPendingSync() && networkManager.isConnected()) {
                Log.d("MyApp", "🔄 Starting background sync for pending notes...")
                delay(2000) // Wait for app to fully initialize

                withContext(Dispatchers.IO) {
                    val syncResult = noteRepository.syncOfflineNotes(context)
                    if (syncResult.isSuccess) {
                        Log.d("MyApp", "✅ Background sync completed successfully")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "🔄 Notes synced successfully", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MyApp", "⚠️ Background sync failed", e)
        }
    }

    // Apply theme to entire app
    ProvideNoteTheme(themeMode = currentTheme) {
        Surface(color = MaterialTheme.colorScheme.surfaceTint) {
            if (isInitializing) {
                // Show loading screen while initializing
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
        composable("addscreen") { AddScreen(navController, noteId = null) }

        composable("welcome") { welcomeScreen(navController) }

        composable("addscreen/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            AddScreen(navController = navController, noteId = noteId)
        }

        composable("viewnote/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            ViewNoteScreen(navController = navController, noteId = noteId)
        }

        composable("noteScreen") { NotesScreen(navController) }


        composable("offlineSyncScreen") {
            com.amvarpvtltd.selfnote.design.OfflineSyncScreen(navController)
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

/**
 * Check Firebase for existing data and sync it to local storage if found
 * Returns true if data was found and synced, false otherwise
 * Now properly handles pending deletions first
 */
suspend fun checkAndSyncFirebaseData(noteRepository: com.amvarpvtltd.selfnote.repository.NoteRepository): Boolean {
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val notesRef: DatabaseReference = database.getReference("notes").child(myGlobalMobileDeviceId)

    return try {
        Log.d("checkAndSyncFirebaseData", "🔍 Checking Firebase for existing notes...")

        // FIRST: Handle any pending deletions before syncing from Firebase
        val context = noteRepository.context
        if (context != null) {
            val offlineManager = OfflineNoteManager(context)
            val pendingDeletions = offlineManager.getPendingDeletions()

            if (pendingDeletions.isNotEmpty()) {
                Log.d("checkAndSyncFirebaseData", "🗑️ Processing ${pendingDeletions.size} pending deletions first...")

                pendingDeletions.forEach { pendingDeletion ->
                    try {
                        notesRef.child(pendingDeletion.noteId).removeValue().await()
                        offlineManager.markDeletionAsSynced(pendingDeletion.noteId)
                        Log.d("checkAndSyncFirebaseData", "✅ Deleted from Firebase: ${pendingDeletion.noteId}")
                    } catch (e: Exception) {
                        Log.e("checkAndSyncFirebaseData", "❌ Failed to delete from Firebase: ${pendingDeletion.noteId}", e)
                    }
                }
            }
        }

        // SECOND: Check if any notes exist in Firebase
        val snapshot = notesRef.get().await()

        if (snapshot.exists() && snapshot.hasChildren()) {
            Log.d("checkAndSyncFirebaseData", "📥 Found ${snapshot.childrenCount} notes in Firebase")

            val cloudNotes = mutableListOf<dataclass>()

            // Process each note from Firebase
            snapshot.children.forEach { childSnapshot ->
                try {
                    val encryptedNote = childSnapshot.getValue(dataclass::class.java)
                    if (encryptedNote != null) {
                        val decryptedNote = dataclass.fromEncryptedData(encryptedNote)
                        cloudNotes.add(decryptedNote)
                        Log.d("checkAndSyncFirebaseData", "✅ Processed note: ${decryptedNote.title}")
                    }
                } catch (e: Exception) {
                    Log.e("checkAndSyncFirebaseData", "❌ Error processing note: ${childSnapshot.key}", e)
                }
            }

            if (cloudNotes.isNotEmpty()) {
                // Sync all notes to local storage
                Log.d("checkAndSyncFirebaseData", "💾 Syncing ${cloudNotes.size} notes to local storage...")

                // Use the repository's fetchNotes method which handles the sync
                val fetchResult = noteRepository.fetchNotes()

                if (fetchResult.isSuccess) {
                    Log.d("checkAndSyncFirebaseData", "✅ Successfully synced Firebase data to local storage")
                    return true
                } else {
                    Log.e("checkAndSyncFirebaseData", "❌ Failed to sync Firebase data: ${fetchResult.exceptionOrNull()?.message}")
                    return false
                }
            } else {
                Log.w("checkAndSyncFirebaseData", "⚠️ No valid notes found in Firebase")
                return false
            }
        } else {
            Log.d("checkAndSyncFirebaseData", "📭 No notes found in Firebase")
            return false
        }
    } catch (e: Exception) {
        Log.e("checkAndSyncFirebaseData", "❌ Error checking Firebase data", e)
        return false
    }
}
