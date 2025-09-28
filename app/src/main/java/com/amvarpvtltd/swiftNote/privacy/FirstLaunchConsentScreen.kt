package com.amvarpvtltd.swiftNote.privacy

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.amvarpvtltd.swiftNote.components.BackgroundProvider
import com.amvarpvtltd.swiftNote.design.NoteTheme

@Composable
fun FirstLaunchConsentScreen(
    onConsentCompleted: () -> Unit
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val consentManager = remember { context.getConsentManager() }
    
    var privacyPolicyRead by remember { mutableStateOf(false) }
    var termsRead by remember { mutableStateOf(false) }
    var consentGiven by remember { mutableStateOf(false) }
    
    val canProceed = privacyPolicyRead && termsRead && consentGiven

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = BackgroundProvider.getBrush())
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // App Icon and Welcome
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = NoteTheme.Primary.copy(alpha = 0.1f)
            ),
            modifier = Modifier.size(80.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    Icons.Default.Note,
                    contentDescription = null,
                    tint = NoteTheme.Primary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Welcome to SwiftNote",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = NoteTheme.OnBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Your privacy-focused note-taking companion",
            style = MaterialTheme.typography.titleMedium,
            color = NoteTheme.OnSurface.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Privacy Commitment Card
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
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = NoteTheme.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Privacy First",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = "SwiftNote is designed with your privacy in mind:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrivacyPoint("🔒", "All notes are encrypted and stored locally")
                    PrivacyPoint("🚫", "No personal data collection or tracking")
                    PrivacyPoint("📱", "You own and control your data")
                    PrivacyPoint("🔄", "Optional sync with end-to-end encryption")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Required Reading Section
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = NoteTheme.Surface,
                contentColor = NoteTheme.OnSurface
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Required Reading",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Privacy Policy Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Checkbox(
                        checked = privacyPolicyRead,
                        onCheckedChange = { privacyPolicyRead = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NoteTheme.Primary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "I have read the Privacy Policy",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        TextButton(
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ayushsingh-22/QuickNote/blob/master/PRIVACY_POLICY.md"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = NoteTheme.Primary
                            )
                        ) {
                            Text("Read Privacy Policy")
                        }
                    }
                }
                
                // Terms of Service Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Checkbox(
                        checked = termsRead,
                        onCheckedChange = { termsRead = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NoteTheme.Primary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "I have read the Terms of Service",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        TextButton(
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ayushsingh-22/QuickNote/blob/master/TERMS_OF_SERVICE.md"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = NoteTheme.Primary
                            )
                        ) {
                            Text("Read Terms of Service")
                        }
                    }
                }
                
                // Consent Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = consentGiven,
                        onCheckedChange = { consentGiven = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NoteTheme.Primary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "I agree to the Privacy Policy and Terms of Service",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Continue Button
        Button(
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                consentManager.acceptPrivacyPolicyAndTerms()
                consentManager.markFirstLaunchCompleted()
                onConsentCompleted()
            },
            enabled = canProceed,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NoteTheme.Primary,
                contentColor = NoteTheme.OnPrimary,
                disabledContainerColor = NoteTheme.OutlineVariant,
                disabledContentColor = NoteTheme.OnSurfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Continue to SwiftNote",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "By continuing, you confirm that you understand how your data is handled and agree to our terms.",
            style = MaterialTheme.typography.bodySmall,
            color = NoteTheme.OnSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun PrivacyPoint(
    icon: String,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = NoteTheme.OnSurfaceVariant
        )
    }
}