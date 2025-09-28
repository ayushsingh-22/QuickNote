package com.amvarpvtltd.swiftNote.privacy

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Consent Manager for GDPR and privacy compliance
 * Manages user consent for various app features and data processing
 */
class ConsentManager private constructor(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "privacy_consent_prefs"
        private const val KEY_FIRST_LAUNCH = "first_launch_completed"
        private const val KEY_PRIVACY_POLICY_ACCEPTED = "privacy_policy_accepted"
        private const val KEY_TERMS_ACCEPTED = "terms_of_service_accepted"
        private const val KEY_ANALYTICS_CONSENT = "analytics_consent"
        private const val KEY_SYNC_CONSENT = "sync_feature_consent"
        private const val KEY_NOTIFICATION_CONSENT = "notification_consent"
        private const val KEY_CAMERA_CONSENT = "camera_permission_consent"
        private const val KEY_CONSENT_VERSION = "consent_version"
        
        // Update this when privacy policy or terms change
        private const val CURRENT_CONSENT_VERSION = 1
        
        @Volatile
        private var INSTANCE: ConsentManager? = null
        
        fun getInstance(context: Context): ConsentManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ConsentManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // State flows for reactive UI updates
    private val _consentState = MutableStateFlow(getCurrentConsentState())
    val consentState: StateFlow<ConsentState> = _consentState.asStateFlow()
    
    data class ConsentState(
        val isFirstLaunch: Boolean = true,
        val privacyPolicyAccepted: Boolean = false,
        val termsAccepted: Boolean = false,
        val analyticsConsent: Boolean = false,
        val syncConsent: Boolean = false,
        val notificationConsent: Boolean = false,
        val cameraConsent: Boolean = false,
        val consentVersion: Int = 0,
        val needsConsentUpdate: Boolean = true
    )
    
    private fun getCurrentConsentState(): ConsentState {
        val currentVersion = prefs.getInt(KEY_CONSENT_VERSION, 0)
        val needsUpdate = currentVersion < CURRENT_CONSENT_VERSION
        
        return ConsentState(
            isFirstLaunch = !prefs.getBoolean(KEY_FIRST_LAUNCH, false),
            privacyPolicyAccepted = prefs.getBoolean(KEY_PRIVACY_POLICY_ACCEPTED, false),
            termsAccepted = prefs.getBoolean(KEY_TERMS_ACCEPTED, false),
            analyticsConsent = prefs.getBoolean(KEY_ANALYTICS_CONSENT, false),
            syncConsent = prefs.getBoolean(KEY_SYNC_CONSENT, false),
            notificationConsent = prefs.getBoolean(KEY_NOTIFICATION_CONSENT, false),
            cameraConsent = prefs.getBoolean(KEY_CAMERA_CONSENT, false),
            consentVersion = currentVersion,
            needsConsentUpdate = needsUpdate
        )
    }
    
    private fun updateState() {
        _consentState.value = getCurrentConsentState()
    }
    
    /**
     * Check if user has completed initial consent flow
     */
    fun hasCompletedInitialConsent(): Boolean {
        val state = _consentState.value
        return !state.isFirstLaunch && 
               state.privacyPolicyAccepted && 
               state.termsAccepted && 
               !state.needsConsentUpdate
    }
    
    /**
     * Mark first launch as completed
     */
    fun markFirstLaunchCompleted() {
        prefs.edit()
            .putBoolean(KEY_FIRST_LAUNCH, true)
            .putInt(KEY_CONSENT_VERSION, CURRENT_CONSENT_VERSION)
            .apply()
        updateState()
    }
    
    /**
     * Accept privacy policy and terms
     */
    fun acceptPrivacyPolicyAndTerms() {
        prefs.edit()
            .putBoolean(KEY_PRIVACY_POLICY_ACCEPTED, true)
            .putBoolean(KEY_TERMS_ACCEPTED, true)
            .putInt(KEY_CONSENT_VERSION, CURRENT_CONSENT_VERSION)
            .apply()
        updateState()
    }
    
    /**
     * Set analytics consent (currently not used but prepared for future)
     */
    fun setAnalyticsConsent(granted: Boolean) {
        prefs.edit()
            .putBoolean(KEY_ANALYTICS_CONSENT, granted)
            .apply()
        updateState()
    }
    
    /**
     * Set sync feature consent
     */
    fun setSyncConsent(granted: Boolean) {
        prefs.edit()
            .putBoolean(KEY_SYNC_CONSENT, granted)
            .apply()
        updateState()
    }
    
    /**
     * Set notification consent
     */
    fun setNotificationConsent(granted: Boolean) {
        prefs.edit()
            .putBoolean(KEY_NOTIFICATION_CONSENT, granted)
            .apply()
        updateState()
    }
    
    /**
     * Set camera permission consent
     */
    fun setCameraConsent(granted: Boolean) {
        prefs.edit()
            .putBoolean(KEY_CAMERA_CONSENT, granted)
            .apply()
        updateState()
    }
    
    /**
     * Check specific consent
     */
    fun hasAnalyticsConsent(): Boolean = _consentState.value.analyticsConsent
    fun hasSyncConsent(): Boolean = _consentState.value.syncConsent
    fun hasNotificationConsent(): Boolean = _consentState.value.notificationConsent
    fun hasCameraConsent(): Boolean = _consentState.value.cameraConsent
    
    /**
     * Reset all consent (for data management)
     */
    fun resetAllConsent() {
        prefs.edit().clear().apply()
        updateState()
    }
    
    /**
     * Update consent version (when privacy policy changes)
     */
    fun updateConsentVersion() {
        prefs.edit()
            .putInt(KEY_CONSENT_VERSION, CURRENT_CONSENT_VERSION)
            .apply()
        updateState()
    }
    
    /**
     * Get consent summary for privacy dashboard
     */
    fun getConsentSummary(): List<ConsentItem> {
        val state = _consentState.value
        return listOf(
            ConsentItem(
                title = "Privacy Policy & Terms",
                description = "Basic app usage agreement",
                isGranted = state.privacyPolicyAccepted && state.termsAccepted,
                isRequired = true,
                canChange = false
            ),
            ConsentItem(
                title = "Notifications",
                description = "Smart reminders for your notes",
                isGranted = state.notificationConsent,
                isRequired = false,
                canChange = true
            ),
            ConsentItem(
                title = "Camera Access",
                description = "QR code scanning for sync",
                isGranted = state.cameraConsent,
                isRequired = false,
                canChange = true
            ),
            ConsentItem(
                title = "Sync Feature",
                description = "Backup and sync across devices",
                isGranted = state.syncConsent,
                isRequired = false,
                canChange = true
            ),
            ConsentItem(
                title = "Analytics",
                description = "Currently not collected",
                isGranted = false,
                isRequired = false,
                canChange = false,
                isDisabled = true
            )
        )
    }
    
    data class ConsentItem(
        val title: String,
        val description: String,
        val isGranted: Boolean,
        val isRequired: Boolean,
        val canChange: Boolean,
        val isDisabled: Boolean = false
    )
}

/**
 * Extension function for easy access in Activities
 */
fun Context.getConsentManager(): ConsentManager = ConsentManager.getInstance(this)