package com.amvarpvtltd.selfnote.auth

import android.content.Context

object DeviceManager {
    /**
     * Backwards-compatible API. Returns stored device id or creates a new one.
     */
    fun getOrCreateDeviceId(context: Context): String {
        return DeviceIdManager.getOrCreateDeviceId(context)
    }

    fun getStoredDeviceId(context: Context): String? {
        return DeviceIdManager.getStoredDeviceId(context)
    }

    fun clearDeviceId(context: Context) {
        DeviceIdManager.clearDeviceId(context)
    }

    /**
     * Mark that onboarding/first-launch completed. Delegates to DeviceIdManager.
     */
    fun markFirstLaunchComplete(context: Context) {
        DeviceIdManager.markFirstLaunchComplete(context)
    }
}
