package com.amvarpvtltd.selfnote.design

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.amvarpvtltd.selfnote.components.ActionButton
import com.amvarpvtltd.selfnote.components.LoadingCard
import com.amvarpvtltd.selfnote.components.NoteScreenBackground
import com.amvarpvtltd.selfnote.ui.theme.Lobster_Font
import kotlinx.coroutines.delay

@Composable
fun welcomeScreen(navController: NavHostController) {
    var isLoading by remember { mutableStateOf(true) }
    val hapticFeedback = LocalHapticFeedback.current

    // Simulated loading
    LaunchedEffect(Unit) {
        delay(2000)
        isLoading = false
    }

    val infiniteTransition = rememberInfiniteTransition(label = "anim")
    val floatingAnimation by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "float"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    val backgroundColor = NoteTheme.Surface
    val textColor = if (isColorDark(backgroundColor)) Color.White else Color.Black

    NoteScreenBackground {
        if (isLoading) {
            LoadingCard()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Main icon
                Card(
                    modifier = Modifier
                        .size(160.dp)
                        .graphicsLayer { translationY = floatingAnimation }
                        .scale(pulseScale),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = NoteTheme.Surface),
                    elevation = CardDefaults.cardElevation(10.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EditNote,
                            contentDescription = "edit note",
                            tint = NoteTheme.Primary,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // App name
                Text(
                    text = "SelfNote",
                    fontFamily = Lobster_Font,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Text(
                    text = "Capture your thoughts, simply and beautifully",
                    color = textColor.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(48.dp))

                // CTA button
                ActionButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        navController.navigate("addscreen")
                    },
                    text = "Create Your First Note",
                    icon = Icons.Default.Add,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            }
        }
    }
}

private fun isColorDark(color: Color): Boolean {
    val luminance = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
    return luminance < 0.5
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun WelcomeScreenPreview() {
    val navController = rememberNavController()
    welcomeScreen(navController = navController)
}