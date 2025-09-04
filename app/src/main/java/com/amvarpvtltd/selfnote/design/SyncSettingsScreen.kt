@file:Suppress("DEPRECATION")

package com.amvarpvtltd.selfnote.design

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.amvarpvtltd.selfnote.auth.PassphraseManager
import com.amvarpvtltd.selfnote.myGlobalMobileDeviceId
import com.amvarpvtltd.selfnote.sync.SyncManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current

    var currentPassphrase by remember { mutableStateOf("") }
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showQRCode by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var showQRScanner by remember { mutableStateOf(false) }
    var inputPassphrase by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var syncStats by remember { mutableStateOf<com.amvarpvtltd.selfnote.sync.SyncStats?>(null) }

    // Load current passphrase on screen load
    LaunchedEffect(Unit) {
        currentPassphrase = PassphraseManager.getStoredPassphrase(context) ?: myGlobalMobileDeviceId
        if (currentPassphrase.isNotEmpty()) {
            // Load sync stats
            scope.launch {
                val statsResult = SyncManager.getSyncStats(currentPassphrase)
                if (statsResult.isSuccess) {
                    syncStats = statsResult.getOrNull()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sync Settings", color = NoteTheme.OnSurface) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = NoteTheme.OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NoteTheme.SurfaceVariant
                )
            )
        }
    ) { paddingValues ->
        // Reworked UI: modern consistent layout using ElevatedCard, tonal buttons and helper sections
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 18.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Device Info Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = NoteTheme.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader(icon = Icons.Default.Smartphone, title = "This Device")
                    Spacer(modifier = Modifier.height(8.dp))

                    if (currentPassphrase.isNotEmpty()) {
                        Text(
                            text = "Passphrase",
                            style = MaterialTheme.typography.bodySmall,
                            color = NoteTheme.OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SelectionContainer(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentPassphrase,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium,
                                    color = NoteTheme.OnSurface
                                )
                            }
                            FilledTonalIconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(currentPassphrase))
                                    Toast.makeText(context, "Passphrase copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.padding(start = 8.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = NoteTheme.SecondaryContainer)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = NoteTheme.OnSecondaryContainer)
                            }
                        }

                        // Stats
                        syncStats?.let { stats ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = NoteTheme.Outline)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatItem(value = stats.totalNotes.toString(), label = "Notes", icon = Icons.Default.Description)
                                StatItem(value = stats.totalReminders.toString(), label = "Reminders", icon = Icons.Default.Notifications)
                                val lastSync = if (stats.lastSyncAt > 0) SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(stats.lastSyncAt)) else "Never"
                                StatItem(value = lastSync, label = "Last Sync", icon = Icons.Default.Sync)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No passphrase found for this device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = NoteTheme.OnSurfaceVariant
                        )
                    }
                }
            }

            // Share & Sync Actions Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = NoteTheme.Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionSection(
                        icon = Icons.Default.Share,
                        title = "Share with Another Device",
                        description = "Use one of the options below to securely transfer your passphrase."
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilledTonalButton(
                                onClick = {
                                    if (currentPassphrase.isNotEmpty()) {
                                        qrCodeBitmap = PassphraseManager.generateQRCode(currentPassphrase)
                                        showQRCode = true
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = NoteTheme.PrimaryContainer,
                                    contentColor = NoteTheme.OnPrimaryContainer
                                )
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp), tint = NoteTheme.Primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Show QR", color = NoteTheme.OnPrimary)
                            }

                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(currentPassphrase))
                                    Toast.makeText(context, "Passphrase copied to clipboard!", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NoteTheme.OnSurface)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp), tint = NoteTheme.OnSurface)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copy", color = NoteTheme.OnSurface)
                            }
                        }
                    }

                    Divider(color = NoteTheme.Outline)

                    ActionSection(
                        icon = Icons.Default.CloudDownload,
                        title = "Import from Another Device",
                        description = "Scan a device QR or enter a passphrase to import notes & reminders."
                    ) {
                        FilledTonalButton(
                            onClick = { showQRScanner = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = NoteTheme.SecondaryContainer,
                                contentColor = NoteTheme.OnSecondaryContainer
                            )
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp), tint = NoteTheme.Primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan and Sync", color = NoteTheme.OnSecondary)
                        }

                        TextButton(onClick = { showSyncDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.textButtonColors(contentColor = NoteTheme.Primary)) {
                            Text("Enter passphrase manually", color = NoteTheme.Primary)
                        }
                    }
                }
            }

            // Info Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = NoteTheme.SurfaceVariant),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "How Sync Works", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = NoteTheme.OnSurface)
                    InfoItem("Each device has a unique passphrase")
                    InfoItem("Sync copies data from one device to another")
                    InfoItem("After sync, devices remain independent")
                    InfoItem("Your data is encrypted and secure")
                }
            }
        }
    }

    // QR Code Dialog
    if (showQRCode && qrCodeBitmap != null) {
        QRCodeDialog(
            bitmap = qrCodeBitmap!!,
            passphrase = currentPassphrase,
            onDismiss = { showQRCode = false }
        )
    }

    // Camera QR Scanner (open directly when user taps Sync Data)
    if (showQRScanner) {
        val scannerContext = LocalContext.current
        QRScannerSection(
            onQRScanned = { qrContent ->
                // Extract passphrase from scanned content before using it
                val extracted = PassphraseManager.extractPassphraseFromQR(qrContent)
                if (extracted == null) {
                    Toast.makeText(scannerContext, "Invalid QR code format", Toast.LENGTH_SHORT).show()
                    showQRScanner = false
                    return@QRScannerSection
                }

                // Immediately attempt verify + sync
                scope.launch {
                    isLoading = true
                    errorMessage = ""
                    try {
                        val passphrase = extracted

                        // Verify passphrase exists
                        val verifyResult = PassphraseManager.verifyPassphrase(passphrase)
                        if (verifyResult.isSuccess && (verifyResult.getOrNull() == true)) {
                            // Perform sync from scanned passphrase to current device
                            val syncResult = SyncManager.syncDataFromPassphrase(scannerContext, passphrase, currentPassphrase)
                            if (syncResult.isSuccess) {
                                val result = syncResult.getOrNull()
                                Toast.makeText(
                                    scannerContext,
                                    "Synced ${result?.syncedNotesCount ?: 0} notes and ${result?.syncedRemindersCount ?: 0} reminders!",
                                    Toast.LENGTH_LONG
                                ).show()

                                // Refresh stats
                                val statsResult = SyncManager.getSyncStats(currentPassphrase)
                                if (statsResult.isSuccess) syncStats = statsResult.getOrNull()
                            } else {
                                errorMessage = syncResult.exceptionOrNull()?.message ?: "Sync failed"
                            }
                        } else {
                            errorMessage = verifyResult.exceptionOrNull()?.message ?: "Passphrase not found. Please check and try again."
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error: ${e.message}"
                    } finally {
                        isLoading = false
                        showQRScanner = false
                    }
                }
            },
            onCancel = {
                showQRScanner = false
            }
        )
    }

    // Sync Dialog
    if (showSyncDialog) {
        SyncFromDeviceDialog(
            inputPassphrase = inputPassphrase,
            onPassphraseChange = {
                inputPassphrase = it.lowercase()
                errorMessage = ""
            },
            errorMessage = errorMessage,
            isLoading = isLoading,
            onSync = {
                scope.launch {
                    if (inputPassphrase.isBlank()) {
                        errorMessage = "Please enter a passphrase"
                        return@launch
                    }

                    if (!PassphraseManager.isValidPassphraseFormat(inputPassphrase)) {
                        errorMessage = "Invalid format. Use: word-word-123"
                        return@launch
                    }

                    isLoading = true
                    errorMessage = ""

                    try {
                        // Verify source passphrase exists
                        val verifyResult = PassphraseManager.verifyPassphrase(inputPassphrase)
                        if (verifyResult.isSuccess) {
                            val exists = verifyResult.getOrNull() ?: false
                            if (exists) {
                                // Sync data from source device to current device
                                val syncResult = SyncManager.syncDataFromPassphrase(
                                    context, inputPassphrase, currentPassphrase
                                )

                                if (syncResult.isSuccess) {
                                    val result = syncResult.getOrNull()
                                    Toast.makeText(
                                        context,
                                        "Synced ${result?.syncedNotesCount ?: 0} notes and ${result?.syncedRemindersCount ?: 0} reminders!",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    showSyncDialog = false
                                    inputPassphrase = ""

                                    // Refresh sync stats
                                    val statsResult = SyncManager.getSyncStats(currentPassphrase)
                                    if (statsResult.isSuccess) {
                                        syncStats = statsResult.getOrNull()
                                    }
                                } else {
                                    errorMessage = "Sync failed: ${syncResult.exceptionOrNull()?.message ?: "Unknown error"}"
                                }
                            } else {
                                errorMessage = "Passphrase not found. Please check and try again."
                            }
                        } else {
                            // Show friendly error returned from verifyPassphrase (permission/network etc.)
                            errorMessage = verifyResult.exceptionOrNull()?.message ?: "Failed to verify passphrase. Please try again."
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            onCancel = {
                showSyncDialog = false
                inputPassphrase = ""
                errorMessage = ""
            }
        )
    }
}

// Helper composables added to keep UI consistent with other screens
@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = NoteTheme.Primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = NoteTheme.OnSurface)
    }
}

@Composable
private fun StatItem(value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = null, tint = NoteTheme.Primary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NoteTheme.OnSurface)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = NoteTheme.OnSurfaceVariant)
    }
}

@Composable
private fun ActionSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = NoteTheme.Primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = NoteTheme.OnSurface)
        }
        Text(description, style = MaterialTheme.typography.bodySmall, color = NoteTheme.OnSurfaceVariant)
        Spacer(modifier = Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun InfoItem(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        Text("•", color = NoteTheme.Primary)
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = NoteTheme.OnSurfaceVariant)
    }
}

@Composable
fun QRCodeDialog(
    bitmap: Bitmap,
    passphrase: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "QR Code for Sync",
                fontWeight = FontWeight.Bold,
                color = NoteTheme.OnSurface
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(200.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Scan this QR code on another device to sync your data",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = NoteTheme.OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = passphrase,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = NoteTheme.OnSurface.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = NoteTheme.Primary)) {
                Text("Close")
            }
        }
    )
}

@Composable
fun SyncFromDeviceDialog(
    inputPassphrase: String,
    onPassphraseChange: (String) -> Unit,
    errorMessage: String,
    isLoading: Boolean,
    onSync: () -> Unit,
    onCancel: () -> Unit
) {
    var showQRScanner by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = "Sync from Another Device",
                fontWeight = FontWeight.Bold,
                color = NoteTheme.OnSurface
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter the passphrase from the other device:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                    color = NoteTheme.OnSurfaceVariant
                )

                // Passphrase Input
            TextField(
                value = inputPassphrase,
                onValueChange = onPassphraseChange,
                placeholder = { Text("Enter passphrase", color = NoteTheme.OnSurfaceVariant) },
                isError = errorMessage.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = NoteTheme.SurfaceVariant,
                    unfocusedContainerColor = NoteTheme.SurfaceVariant,
                    focusedTextColor = NoteTheme.OnSurface,
                    unfocusedTextColor = NoteTheme.OnSurface,
                    cursorColor = NoteTheme.Primary,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

                // Error message
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = NoteTheme.Error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sync button
                Button(
                    onClick = onSync,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = NoteTheme.Primary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = NoteTheme.OnPrimary
                        )
                    } else {
                        Text("Sync Data", color = NoteTheme.OnPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cancel button
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NoteTheme.OnSurface)
                ) {
                    Text("Cancel", color = NoteTheme.OnSurface)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // QR Code Scanner button
                Button(
                    onClick = { showQRScanner = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NoteTheme.Secondary)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                        tint = NoteTheme.OnSecondary
                    )
                    Text("Scan QR Code", color = NoteTheme.OnSecondary)
                }
            }
        },
        confirmButton = {
            // No separate confirm button needed; actions are inside content.
            // Provide an empty composable to satisfy AlertDialog signature.
            Box {}
        },
        dismissButton = {
            TextButton(onClick = onCancel, colors = ButtonDefaults.textButtonColors(contentColor = NoteTheme.Primary)) {
                Text("Close", color = NoteTheme.Primary)
            }
        }
    )

    // Camera QR Scanner
    if (showQRScanner) {
        val scannerContext = LocalContext.current
        QRScannerSection(
            onQRScanned = { qrContent ->
                // Extract passphrase from scanned content before using it
                val extracted = PassphraseManager.extractPassphraseFromQR(qrContent)
                if (extracted == null) {
                    Toast.makeText(scannerContext, "Invalid QR code format", Toast.LENGTH_SHORT).show()
                    showQRScanner = false
                    return@QRScannerSection
                }

                // Immediately attempt verify + sync
                onPassphraseChange(extracted)
                onSync()
            },
            onCancel = {
                showQRScanner = false
            }
        )
    }
}
