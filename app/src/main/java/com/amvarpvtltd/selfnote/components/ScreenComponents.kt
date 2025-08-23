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
fun NoteScreenBackground(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NoteTheme.Background,
                        NoteTheme.SurfaceVariant.copy(alpha = 0.3f),
                        NoteTheme.Background
                    )
                )
            )
    ) {
        content()
    }
}

@Composable
fun LoadingCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(Constants.CORNER_RADIUS_LARGE.dp),
            colors = CardDefaults.cardColors(containerColor = NoteTheme.Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(Constants.PADDING_XL.dp)
            ) {
                CircularProgressIndicator(
                    color = NoteTheme.Primary,
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(Constants.PADDING_MEDIUM.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = NoteTheme.OnSurface,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = NoteTheme.OnSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    description: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(Constants.CORNER_RADIUS_LARGE.dp),
            colors = CardDefaults.cardColors(containerColor = NoteTheme.Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.padding(Constants.PADDING_LARGE.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(Constants.PADDING_XL.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    NoteTheme.Primary.copy(alpha = 0.2f),
                                    NoteTheme.Primary.copy(alpha = 0.05f)
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = NoteTheme.Primary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(Constants.PADDING_LARGE.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = NoteTheme.OnSurface,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(Constants.PADDING_SMALL.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = NoteTheme.OnSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(Constants.PADDING_LARGE.dp))

                Button(
                    onClick = onButtonClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NoteTheme.Primary,
                        contentColor = NoteTheme.OnPrimary
                    ),
                    shape = RoundedCornerShape(Constants.CORNER_RADIUS_MEDIUM.dp)
                ) {
                    Text(
                        text = buttonText,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fabPressed by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current

    val fabScale by animateFloatAsState(
        targetValue = if (fabPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "fab_scale"
    )

    ExtendedFloatingActionButton(
        onClick = {
            fabPressed = true
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = modifier
            .scale(fabScale)
            .shadow(8.dp, RoundedCornerShape(Constants.CORNER_RADIUS_LARGE.dp)),
        containerColor = NoteTheme.Primary,
        contentColor = NoteTheme.OnPrimary,
        shape = RoundedCornerShape(Constants.CORNER_RADIUS_LARGE.dp),
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Icon(
            Icons.Outlined.Add,
            contentDescription = "Add Note",
            modifier = Modifier.size(Constants.ICON_SIZE_LARGE.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "New Note",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }

    LaunchedEffect(fabPressed) {
        if (fabPressed) {
            delay(Constants.SPRING_ANIMATION_DELAY.toLong())
            fabPressed = false
        }
    }
}
