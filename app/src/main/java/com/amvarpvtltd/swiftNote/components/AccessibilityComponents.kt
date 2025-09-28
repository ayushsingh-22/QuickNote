package com.amvarpvtltd.swiftNote.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.semantics.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp

/**
 * Accessibility-enhanced components for Play Store compliance
 * Implements proper content descriptions, semantic roles, and keyboard navigation
 */

@Composable
fun AccessibleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String,
    icon: ImageVector? = null,
    text: String
) {
    Button(
        onClick = onClick,
        modifier = modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Button
            if (!enabled) {
                this.disabled()
            }
        },
        enabled = enabled
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null, // Null because parent has description
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text)
    }
}

@Composable
fun AccessibleIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Button
            if (!enabled) {
                this.disabled()
            }
        },
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null // Null because parent has description
        )
    }
}

@Composable
fun AccessibleCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    isClickable: Boolean = onClick != null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (isClickable && onClick != null) {
        modifier
            .clickable { onClick() }
            .semantics {
                this.role = Role.Button
                contentDescription?.let { this.contentDescription = it }
            }
    } else {
        modifier
    }

    Card(
        modifier = cardModifier,
        content = content
    )
}

@Composable
fun AccessibleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    supportingText: String? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = placeholder?.let { { Text(it) } },
            isError = isError,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    this.contentDescription = label
                    if (isError && errorMessage != null) {
                        this.error(errorMessage)
                    }
                    supportingText?.let { 
                        this.text = AnnotatedString("$label. $it")
                    }
                },
            supportingText = supportingText?.let { { Text(it) } }
        )
        
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(start = 16.dp, top = 4.dp)
                    .semantics {
                        this.role = Role.Text
                        this.contentDescription = "Error: $errorMessage"
                    }
            )
        }
    }
}

@Composable
fun AccessibleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    description: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .semantics {
                this.role = Role.Switch
                this.contentDescription = description ?: label
                this.stateDescription = if (checked) "On" else "Off"
                if (!enabled) {
                    this.disabled()
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.semantics { 
                    this.heading()
                }
            )
            description?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.semantics {
                        this.role = Role.Text
                    }
                )
            }
        }
        
        Switch(
            checked = checked,
            onCheckedChange = null, // Handled by parent click
            enabled = enabled,
            modifier = Modifier.semantics {
                // Clear semantics since parent handles them
                this.clearAndSetSemantics { }
            }
        )
    }
}

@Composable
fun AccessibleDialog(
    onDismissRequest: () -> Unit,
    title: String,
    text: String? = null,
    confirmButtonText: String = "OK",
    dismissButtonText: String? = null,
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    icon: ImageVector? = null
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Row {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text(
                    text = title,
                    modifier = Modifier.semantics {
                        this.heading()
                    }
                )
            }
        },
        text = text?.let { 
            {
                Text(
                    text = it,
                    modifier = Modifier.semantics {
                        this.role = Role.Text
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.semantics {
                    this.contentDescription = "$confirmButtonText button"
                    this.role = Role.Button
                }
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = if (dismissButtonText != null && onDismiss != null) {
            {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.semantics {
                        this.contentDescription = "$dismissButtonText button"
                        this.role = Role.Button
                    }
                ) {
                    Text(dismissButtonText)
                }
            }
        } else null,
        modifier = Modifier.semantics {
            this.role = Role.Dialog
            this.contentDescription = "Dialog: $title"
        }
    )
}

@Composable
fun AccessibleTabRow(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<String>,
    modifier: Modifier = Modifier
) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier.semantics {
            this.role = Role.TabList
            this.contentDescription = "Tab navigation with ${tabs.size} tabs"
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = { Text(title) },
                modifier = Modifier.semantics {
                    this.role = Role.Tab
                    this.contentDescription = "Tab $title"
                    if (selectedTabIndex == index) {
                        this.selected = true
                    }
                }
            )
        }
    }
}

/**
 * Helper function to announce content changes to accessibility services
 */
fun announceForAccessibility(text: String): SemanticsPropertyReceiver.() -> Unit = {
    this.liveRegion = LiveRegionMode.Polite
    this.contentDescription = text
}