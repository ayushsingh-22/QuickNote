package com.amvarpvtltd.selfnote

import android.app.Application
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        AppContext.appContext = applicationContext

        // Ensure we have a stable device/account id available globally for older code paths
        try {
            val storedPass = com.amvarpvtltd.selfnote.auth.PassphraseManager.getStoredPassphrase(this)
            val deviceId = com.amvarpvtltd.selfnote.auth.DeviceManager.getOrCreateDeviceId(this)
            myGlobalMobileDeviceId = storedPass ?: deviceId
        } catch (e: Exception) {
            // If anything fails, fallback to a random UUID
            myGlobalMobileDeviceId = java.util.UUID.randomUUID().toString()
        }
    }
}
