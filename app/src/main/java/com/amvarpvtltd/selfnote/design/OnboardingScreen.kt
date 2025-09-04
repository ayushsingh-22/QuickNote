package com.amvarpvtltd.selfnote.design

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.amvarpvtltd.selfnote.auth.DeviceManager
import com.amvarpvtltd.selfnote.auth.PassphraseManager
import com.amvarpvtltd.selfnote.sync.SyncManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showQRScanner by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Mark first launch as complete
        DeviceManager.markFirstLaunchComplete(context)
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Welcome Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 32.dp)
            ) {
                Text(
                    text = "Welcome to QuickNote",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Choose how you'd like to start:",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Continue Fresh
            ElevatedCard(
                onClick = {
                    scope.launch {
                        isLoading = true
                        try {
                            // Initialize with fresh device ID as the passphrase for this device
                            val deviceId = DeviceManager.getOrCreateDeviceId(context)
                            PassphraseManager.storePassphrase(context, deviceId).getOrThrow()

                            // After storing passphrase, check Firebase for existing notes under this device
                            // If notes exist remotely for this device, import them into local DB so user immediately sees them
                            try {
                                val syncResult = SyncManager.syncDataFromPassphrase(context, deviceId, deviceId)
                                if (syncResult.isSuccess) {
                                    // imported successfully (or nothing to import)
                                    android.util.Log.d("OnboardingScreen", "Imported remote notes for device: $deviceId")
                                } else {
                                    android.util.Log.w("OnboardingScreen", "No remote notes or import failed for device: $deviceId - ${syncResult.exceptionOrNull()?.message}")
                                }
                            } catch (e: Exception) {
                                android.util.Log.w("OnboardingScreen", "Remote import check failed for device: $deviceId", e)
                            }

                            // Navigate to main screen
                            navController.navigate("main") {
                                popUpTo("onboarding") { inclusive = true }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "Setup failed: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Start Fresh",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Create a new notes collection",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Restore Existing
            ElevatedCard(
                onClick = { showQRScanner = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Restore Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sync notes from another device",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator()
            }
        }
    }

    // QR Scanner for restore
    if (showQRScanner) {
        QRScannerSection(
            onQRScanned = { qrContent ->
                scope.launch {
                    isLoading = true
                    try {
                        val sourcePassphrase = PassphraseManager.extractPassphraseFromQR(qrContent)
                        if (sourcePassphrase == null) {
                            Toast.makeText(
                                context,
                                "Invalid QR code format",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@launch
                        }

                        // Create/ensure current device passphrase
                        val currentPassphrase = DeviceManager.getOrCreateDeviceId(context)
                        PassphraseManager.storePassphrase(context, currentPassphrase).getOrThrow()

                        // Sync data from source device into this device
                        val result = SyncManager.syncDataFromPassphrase(
                            context,
                            sourcePassphrase,
                            currentPassphrase
                        )
                        if (result.isFailure) {
                            Toast.makeText(
                                context,
                                "Restore failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        // Navigate to main screen
                        navController.navigate("main") {
                            popUpTo("onboarding") { inclusive = true }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "Restore failed: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    } finally {
                        isLoading = false
                        showQRScanner = false
                    }
                }
            },
            onCancel = { showQRScanner = false }
        )
    }
}
