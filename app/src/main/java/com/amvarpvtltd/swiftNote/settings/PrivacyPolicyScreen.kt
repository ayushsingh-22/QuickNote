package com.amvarpvtltd.swiftNote.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.amvarpvtltd.swiftNote.components.BackgroundProvider
import com.amvarpvtltd.swiftNote.design.NoteTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    var showDataExportDialog by remember { mutableStateOf(false) }
    var showAccountDeletionDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Privacy & Data",
                        color = NoteTheme.OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            navController.navigateUp()
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NoteTheme.OnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NoteTheme.SurfaceVariant
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = BackgroundProvider.getBrush())
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Privacy Overview Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = NoteTheme.Surface,
                        contentColor = NoteTheme.OnSurface
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = NoteTheme.PrimaryContainer.copy(alpha = 0.5f)
                                )
                            ) {
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = NoteTheme.Primary,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Your Privacy Matters",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NoteTheme.OnSurface
                            )
                        }

                        Text(
                            text = "SwiftNote is designed with privacy at its core. Your notes are encrypted, stored locally, and you have full control over your data.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NoteTheme.OnSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Privacy highlights
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PrivacyHighlight("🔒", "Local encryption of all note content")
                            PrivacyHighlight("📱", "Data stored on your device only")
                            PrivacyHighlight("🚫", "No tracking or analytics")
                            PrivacyHighlight("🔄", "Optional sync with end-to-end encryption")
                        }
                    }
                }

                // Data Rights Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = NoteTheme.Surface,
                        contentColor = NoteTheme.OnSurface
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = NoteTheme.SecondaryContainer.copy(alpha = 0.5f)
                                )
                            ) {
                                Icon(
                                    Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = NoteTheme.Secondary,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Your Data Rights",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NoteTheme.OnSurface
                            )
                        }

                        Text(
                            text = "In compliance with GDPR and privacy regulations, you have the following rights:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NoteTheme.OnSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Data export button
                        Button(
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                showDataExportDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NoteTheme.Primary,
                                contentColor = NoteTheme.OnPrimary
                            )
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export My Data", fontWeight = FontWeight.Medium)
                        }

                        // Data management navigation
                        OutlinedButton(
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                navController.navigate("data_management")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = NoteTheme.Primary
                            )
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Manage My Data", fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Policy Links Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = NoteTheme.Surface,
                        contentColor = NoteTheme.OnSurface
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = NoteTheme.TertiaryContainer.copy(alpha = 0.5f)
                                )
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = NoteTheme.Tertiary,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Legal Documents",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NoteTheme.OnSurface
                            )
                        }

                        // Privacy Policy Link
                        OutlinedButton(
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                // TODO: Replace with actual privacy policy URL
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ayushsingh-22/QuickNote/blob/master/PRIVACY_POLICY.md"))
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Unable to open privacy policy", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = NoteTheme.Primary
                            )
                        ) {
                            Icon(Icons.Default.Policy, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Privacy Policy", fontWeight = FontWeight.Medium)
                        }

                        // Terms of Service Link
                        OutlinedButton(
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                // TODO: Replace with actual terms of service URL
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ayushsingh-22/QuickNote/blob/master/TERMS_OF_SERVICE.md"))
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Unable to open terms of service", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = NoteTheme.Primary
                            )
                        ) {
                            Icon(Icons.Default.Gavel, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Terms of Service", fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Contact & Support Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = NoteTheme.SurfaceVariant.copy(alpha = 0.7f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ContactSupport,
                                contentDescription = null,
                                tint = NoteTheme.Primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Questions or Concerns?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NoteTheme.OnSurface
                            )
                        }

                        Text(
                            text = "If you have any questions about your privacy or how your data is handled, please contact us through GitHub issues or repository discussions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NoteTheme.OnSurface
                        )

                        OutlinedButton(
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ayushsingh-22/QuickNote/issues"))
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Unable to open GitHub", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = NoteTheme.Primary
                            )
                        ) {
                            Icon(Icons.Default.BugReport, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Contact Support", fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }

    // Data Export Dialog
    if (showDataExportDialog) {
        AlertDialog(
            onDismissRequest = { showDataExportDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = NoteTheme.Surface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = null,
                        tint = NoteTheme.Primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Export Your Data",
                        fontWeight = FontWeight.Bold,
                        color = NoteTheme.OnSurface
                    )
                }
            },
            text = {
                Text(
                    text = "This feature will allow you to export all your notes and data in a portable format. This functionality is coming in a future update.\n\nCurrently, you can use the sync feature to backup your data to another device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NoteTheme.OnSurfaceVariant,
                    textAlign = TextAlign.Start
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDataExportDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NoteTheme.Primary,
                        contentColor = NoteTheme.OnPrimary
                    )
                ) {
                    Text("Got it", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDataExportDialog = false
                        navController.navigate("sync_settings")
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NoteTheme.Primary
                    )
                ) {
                    Text("Use Sync", fontWeight = FontWeight.Medium)
                }
            }
        )
    }
}

@Composable
private fun PrivacyHighlight(
    icon: String,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = NoteTheme.OnSurfaceVariant
        )
    }
}