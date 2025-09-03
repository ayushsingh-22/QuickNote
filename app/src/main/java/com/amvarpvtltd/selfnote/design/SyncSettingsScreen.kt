package com.amvarpvtltd.selfnote.design

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                title = { Text("Sync Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current Device Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Smartphone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "This Device",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (currentPassphrase.isNotEmpty()) {
                        Text(
                            text = "Passphrase:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentPassphrase,
                                style = MaterialTheme.typography.bodyLarge,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(currentPassphrase))
                                    Toast.makeText(context, "Passphrase copied!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sync Stats
                        syncStats?.let { stats ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "${stats.totalNotes}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Notes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "${stats.totalReminders}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Reminders",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                                Column {
                                    Text(
                                        text = if (stats.lastSyncAt > 0) {
                                            SimpleDateFormat("MMM dd", Locale.getDefault())
                                                .format(Date(stats.lastSyncAt))
                                        } else "Never",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Last Sync",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Share Passphrase Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Share with Another Device",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Use these options to sync your data to another device:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                if (currentPassphrase.isNotEmpty()) {
                                    qrCodeBitmap = PassphraseManager.generateQRCode(currentPassphrase)
                                    showQRCode = true
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Show QR")
                        }

                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(currentPassphrase))
                                Toast.makeText(context, "Passphrase copied to clipboard!", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Copy")
                        }
                    }
                }
            }

            // Sync from Another Device Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Sync from Another Device",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Import notes and reminders from another device:",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = { showQRScanner = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Sync Data")
                    }
                }
            }

            // Info Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "ℹ️ How Sync Works",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "• Each device has a unique passphrase\n" +
                                "• Sync copies data from one device to another\n" +
                                "• After sync, devices remain independent\n" +
                                "• Your data is encrypted and secure",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                fontWeight = FontWeight.Bold
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
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = passphrase,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
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
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter the passphrase from the other device:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Passphrase Input
                TextField(
                    value = inputPassphrase,
                    onValueChange = onPassphraseChange,
                    placeholder = { Text("Enter passphrase") },
                    isError = errorMessage.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Error message
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sync button
                Button(
                    onClick = onSync,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Sync Data")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cancel button
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // QR Code Scanner button
                Button(
                    onClick = { showQRScanner = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Scan QR Code")
                }
            }
        },
        confirmButton = {
            // No separate confirm button needed; actions are inside content.
            // Provide an empty composable to satisfy AlertDialog signature.
            Box {}
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Close")
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
