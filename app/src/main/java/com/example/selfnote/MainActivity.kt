package com.example.selfnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.selfnote.design.welcome
import com.example.selfnote.ui.theme.SelfNoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SelfNoteTheme {
                Scaffold(modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Green) { innerPadding ->
                    MyApp()
                }
            }
        }
    }
}
