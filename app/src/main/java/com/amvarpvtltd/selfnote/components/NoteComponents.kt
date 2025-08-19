package com.amvarpvtltd.selfnote.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.amvarpvtltd.selfnote.design.NoteTheme
import com.amvarpvtltd.selfnote.utils.Constants
import com.amvarpvtltd.selfnote.utils.UIUtils
import com.amvarpvtltd.selfnote.utils.ValidationUtils

@Composable
fun NoteInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    maxLength: Int,
    isValid: Boolean,
    validationMessage: String,
    placeholder: String,
    singleLine: Boolean = true,
    focusRequester: FocusRequester? = null,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val progress = UIUtils.calculateProgress(value.length, maxLength)
    val countColor by animateColorAsState(
        targetValue = UIUtils.getProgressColor(value.length),
        animationSpec = UIUtils.getColorAnimationSpec(),
        label = "count_color"
    )

    Card(
        modifier = modifier.animateContentSize(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFocused) 8.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(containerColor = NoteTheme.Surface),
        shape = RoundedCornerShape(Constants.CORNER_RADIUS_LARGE.dp)
    ) {
        Column(modifier = Modifier.padding(Constants.CORNER_RADIUS_LARGE.dp)) {
            // Header with icon, label and progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = NoteTheme.Primary,
                        modifier = Modifier.size(Constants.ICON_SIZE_MEDIUM.dp)
                    )
                    Spacer(modifier = Modifier.width(Constants.PADDING_SMALL.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        color = NoteTheme.OnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(Constants.PROGRESS_INDICATOR_SIZE.dp)
                            .background(
                                color = countColor.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = countColor,
                            strokeWidth = 2.dp
                        )
                    }
                    Spacer(modifier = Modifier.width(Constants.PADDING_SMALL.dp))
                    Text(
                        text = UIUtils.formatCharacterCount(value.length, maxLength),
                        style = MaterialTheme.typography.labelMedium,
                        color = countColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(Constants.PADDING_MEDIUM.dp))

            // Text field
            OutlinedTextField(
                value = value,
                onValueChange = { if (it.length <= maxLength) onValueChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                    .onFocusChanged { isFocused = it.isFocused },
                placeholder = { Text(placeholder, color = NoteTheme.OnSurfaceVariant) },
                singleLine = singleLine,
                keyboardOptions = KeyboardOptions(imeAction = if (singleLine) ImeAction.Next else ImeAction.Done),
                keyboardActions = keyboardActions,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = countColor,
                    unfocusedBorderColor = NoteTheme.OnSurfaceVariant.copy(alpha = 0.3f),
                    cursorColor = NoteTheme.Primary,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                shape = RoundedCornerShape(Constants.CORNER_RADIUS_SMALL.dp)
            )

            // Validation message
            AnimatedVisibility(
                visible = !isValid,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Row(
                    modifier = Modifier.padding(top = Constants.PADDING_SMALL.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = countColor,
                        modifier = Modifier.size(Constants.ICON_SIZE_SMALL.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = validationMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = countColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButton(
    onClick: () -> Unit,
    text: String,
    icon: ImageVector,
    isLoading: Boolean = false,
    loadingText: String = "Loading...",
    modifier: Modifier = Modifier,
    containerColor: Color = NoteTheme.Primary,
    contentColor: Color = NoteTheme.OnPrimary
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = UIUtils.getSpringAnimationSpec(),
        label = "button_scale"
    )

    ExtendedFloatingActionButton(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier.scale(scale),
        containerColor = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(Constants.CORNER_RADIUS_LARGE.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(Constants.ICON_SIZE_MEDIUM.dp),
                strokeWidth = 2.dp,
                color = contentColor
            )
            Spacer(modifier = Modifier.width(Constants.CORNER_RADIUS_SMALL.dp))
            Text(loadingText, fontWeight = FontWeight.Bold)
        } else {
            Icon(icon, contentDescription = null, modifier = Modifier.size(Constants.ICON_SIZE_LARGE.dp))
            Spacer(modifier = Modifier.width(Constants.CORNER_RADIUS_SMALL.dp))
            Text(text, fontWeight = FontWeight.Bold)
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(Constants.SPRING_ANIMATION_DELAY.toLong())
            isPressed = false
        }
    }
}

@Composable
fun IconActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onClick() },
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Removed shadow
    ) {
        Box(
            modifier = Modifier.padding(Constants.PADDING_SMALL.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(Constants.ICON_SIZE_LARGE.dp)
            )
        }
    }
}
