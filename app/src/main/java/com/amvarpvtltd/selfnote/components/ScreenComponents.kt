package com.amvarpvtltd.selfnote.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.amvarpvtltd.selfnote.design.NoteTheme
import com.amvarpvtltd.selfnote.ui.theme.Lobster_Font
import com.amvarpvtltd.selfnote.utils.Constants
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteTopAppBar(
    title: String,
    isLoading: Boolean = false,
    navController: NavHostController? = null,
    showDeleteAction: Boolean = false,
    onDeleteClick: () -> Unit = {},
    onRefresh: (() -> Unit)? = null
) {
    val hapticFeedback = LocalHapticFeedback.current

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = NoteTheme.OnSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = if (title == "My Notes") Lobster_Font else null,
                    fontSize = if (title == "My Notes") 40.sp else 20.sp
                )

                if (isLoading) {
                    Spacer(modifier = Modifier.width(Constants.CORNER_RADIUS_SMALL.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(Constants.ICON_SIZE_MEDIUM.dp),
                        strokeWidth = 2.dp,
                        color = NoteTheme.Primary
                    )
                }
            }
        },
        navigationIcon = {
            if (navController != null) {
                IconActionButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        navController.navigateUp()
                    },
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    containerColor = NoteTheme.Primary.copy(alpha = 0.1f),
                    contentColor = NoteTheme.Primary
                )
            }
        },
        actions = {
            if (showDeleteAction) {
                IconActionButton(
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDeleteClick()
                    },
                    icon = Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    containerColor = NoteTheme.Error.copy(alpha = 0.1f),
                    contentColor = NoteTheme.Error
                )
            }

            if (onRefresh != null) {
                RefreshButton(onRefresh = onRefresh, isLoading = isLoading)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
private fun RefreshButton(onRefresh: () -> Unit, isLoading: Boolean) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isLoading) 360f else 0f,
        animationSpec = if (isLoading) {
            infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        } else spring(),
        label = "refresh_rotation"
    )

    val scaleAnimation by animateFloatAsState(
        targetValue = if (isLoading) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "refresh_scale"
    )

    Card(
        modifier = Modifier
            .scale(scaleAnimation)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onRefresh() },
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isLoading) NoteTheme.Secondary.copy(alpha = 0.1f)
            else NoteTheme.Primary.copy(alpha = 0.1f)
        ),

    ) {
        Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center)
                    .graphicsLayer(rotationZ = rotationAngle),
                tint = if (isLoading) NoteTheme.Secondary else NoteTheme.Primary
            )
        }
    }
}

@Composable
fun AnimatedFloatingActionButton(
    onClick: () -> Unit,
    icon: ImageVector = Icons.Default.Add,
    text: String? = null,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "fab_scale"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 12.dp,
        animationSpec = spring(),
        label = "fab_elevation"
    )

    if (text != null) {
        ExtendedFloatingActionButton(
            onClick = {
                isPressed = true
                onClick()
            },
            modifier = modifier
                .scale(scale)
                .shadow(shadowElevation, RoundedCornerShape(Constants.CORNER_RADIUS_LARGE.dp)),
            containerColor = NoteTheme.Primary,
            contentColor = NoteTheme.OnPrimary,
            shape = RoundedCornerShape(Constants.CORNER_RADIUS_LARGE.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
            if (text.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(text, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        FloatingActionButton(
            onClick = {
                isPressed = true
                onClick()
            },
            modifier = modifier
                .size(64.dp)
                .scale(scale)
                .shadow(shadowElevation, CircleShape),
            shape = CircleShape,
            containerColor = NoteTheme.Primary,
            contentColor = NoteTheme.OnPrimary
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

@Composable
fun NoteScreenBackground(content: @Composable () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient"
    )

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            NoteTheme.Background,
            NoteTheme.SurfaceVariant.copy(alpha = 0.3f + animatedOffset * 0.1f),
            NoteTheme.Background
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        content()
    }
}
