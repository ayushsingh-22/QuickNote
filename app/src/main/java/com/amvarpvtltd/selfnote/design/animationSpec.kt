package com.amvarpvtltd.selfnote.design

import androidx.compose.animation.core.*
import androidx.compose.ui.unit.dp

object AnimationSpec {
    // Duration constants
    const val FAST_ANIMATION_DURATION = 150
    const val NORMAL_ANIMATION_DURATION = 300
    const val SLOW_ANIMATION_DURATION = 500

    // Common animation specs
    val fastSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    )

    val normalSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val slowSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )

    // Tween animations
    val fastTween = tween<Float>(
        durationMillis = FAST_ANIMATION_DURATION,
        easing = FastOutSlowInEasing
    )

    val normalTween = tween<Float>(
        durationMillis = NORMAL_ANIMATION_DURATION,
        easing = FastOutSlowInEasing
    )

    val slowTween = tween<Float>(
        durationMillis = SLOW_ANIMATION_DURATION,
        easing = FastOutSlowInEasing
    )

    // Scale animations for buttons and interactive elements
    val scaleAnimation = keyframes<Float> {
        durationMillis = FAST_ANIMATION_DURATION
        0.95f at 50
        1.0f at FAST_ANIMATION_DURATION
    }

    // Elevation animation - fixed type
    val elevationAnimation = tween<Float>(
        durationMillis = NORMAL_ANIMATION_DURATION,
        easing = FastOutSlowInEasing
    )
}
