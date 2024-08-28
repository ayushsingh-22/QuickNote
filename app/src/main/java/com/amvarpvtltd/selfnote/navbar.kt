package com.amvarpvtltd.selfnote

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.amvarpvtltd.selfnote.design.AddScreen
import com.amvarpvtltd.selfnote.design.NotesScreen
import com.amvarpvtltd.selfnote.design.welcome
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import myGlobalMobileDeviceId

@Composable
fun MyApp() {
    val navController = rememberNavController()
    myGlobalMobileDeviceId = generateUniqueDeviceId(context = LocalContext.current)
    LaunchedEffect(Unit) {
        val hasData = withContext(Dispatchers.IO) { checkFirebaseData() }
        val startDestination = if (hasData) "noteScreen" else "welcome"
        navController.navigate(startDestination) {
            popUpTo("welcome") { inclusive = true }
        }
    }

    Surface(color = MaterialTheme.colorScheme.surfaceTint) {
        NavigationComponent(navController)
    }
}

@Composable
fun NavigationComponent(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "welcome") {
        composable("addscreen") { AddScreen(navController, noteId = null) }

        composable("welcome") {
            welcome(navController)
        }

        composable("addscreen/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            AddScreen(navController = navController, noteId = noteId)
        }

        composable("noteScreen") {
            NotesScreen(navController)
        }
    }
}

suspend fun checkFirebaseData(): Boolean {

    val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    val notesRef: DatabaseReference = database.getReference("notes").child(myGlobalMobileDeviceId)

    return try {

        val snapshot = notesRef.limitToFirst(1).get().await()
        snapshot.exists()
    }
    catch (e: Exception) {

        e.printStackTrace()
        false
    }
}
