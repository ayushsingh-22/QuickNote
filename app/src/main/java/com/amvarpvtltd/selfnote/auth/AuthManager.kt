package com.amvarpvtltd.selfnote.auth

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.amvarpvtltd.selfnote.myGlobalMobileDeviceId
import com.amvarpvtltd.selfnote.repository.NoteRepository
import com.amvarpvtltd.selfnote.security.HashUtils
import com.amvarpvtltd.selfnote.sync.SyncManager
import com.amvarpvtltd.selfnote.utils.QRUtils
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

object AuthManager {
    private const val TAG = "AuthManager"
    private const val PREFS = "auth_prefs"
    private const val KEY_DEVICE_ID = "device_id"

    fun getStoredDeviceId(context: Context): String? {
        // First check if we have a passphrase (new system)
        val passphrase = PassphraseManager.getStoredPassphrase(context)
        if (!passphrase.isNullOrEmpty()) {
            myGlobalMobileDeviceId = passphrase
            return passphrase
        }

        // Fallback to old device ID system for backward compatibility
        val id = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_DEVICE_ID, null)
        if (!id.isNullOrEmpty()) {
            myGlobalMobileDeviceId = id
        }
        return id
    }

    fun storeDeviceId(context: Context, deviceId: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_DEVICE_ID, deviceId).apply()
        myGlobalMobileDeviceId = deviceId
    }

    suspend fun continueAnonymously(context: Context): String = withContext(Dispatchers.IO) {
        // Check if we already have a passphrase
        val existingPassphrase = PassphraseManager.getStoredPassphrase(context)
        if (!existingPassphrase.isNullOrEmpty()) {
            myGlobalMobileDeviceId = existingPassphrase
            return@withContext existingPassphrase
        }

        // Generate new passphrase for this device
        val newPassphrase = PassphraseManager.generatePassphrase()

        // Store passphrase locally and in Firebase
        val storeResult = PassphraseManager.storePassphrase(context, newPassphrase)
        if (storeResult.isSuccess) {
            myGlobalMobileDeviceId = newPassphrase

            // Upload any existing local data to Firebase
            SyncManager.uploadLocalDataToFirebase(context, newPassphrase)

            Log.d(TAG, "New anonymous user created with passphrase: $newPassphrase")
            return@withContext newPassphrase
        } else {
            // Fallback to old UUID system if Firebase fails
            val fallbackId = UUID.randomUUID().toString()
            storeDeviceId(context, fallbackId)
            Log.w(TAG, "Firebase failed, using fallback UUID: $fallbackId")
            return@withContext fallbackId
        }
    }

    suspend fun loginWithPassphrase(context: Context, passphrase: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Verify passphrase exists in Firebase
            val verifyResult = PassphraseManager.verifyPassphrase(passphrase)
            if (verifyResult.isSuccess && verifyResult.getOrNull() == true) {
                // Generate new passphrase for this device
                val newPassphrase = PassphraseManager.generatePassphrase()

                // Store new passphrase
                val storeResult = PassphraseManager.storePassphrase(context, newPassphrase)
                if (storeResult.isSuccess) {
                    myGlobalMobileDeviceId = newPassphrase

                    // Sync data from source device
                    val syncResult = SyncManager.syncDataFromPassphrase(context, passphrase, newPassphrase)
                    if (syncResult.isSuccess) {
                        Log.d(TAG, "Successfully logged in and synced with passphrase: $passphrase")
                        Result.success(newPassphrase)
                    } else {
                        Result.failure(Exception("Sync failed: ${syncResult.exceptionOrNull()?.message}"))
                    }
                } else {
                    Result.failure(Exception("Failed to store new passphrase"))
                }
            } else {
                Result.failure(IllegalArgumentException("Invalid passphrase"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login with passphrase failed", e)
            Result.failure(e)
        }
    }

    suspend fun loginWithDeviceId(context: Context, deviceId: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            // This is for backward compatibility with old device ID system
            storeDeviceId(context, deviceId)
            val repo = NoteRepository(context)
            repo.fetchNotes()
            Result.success(deviceId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate a human-friendly alphanumeric passphrase and store its SHA-256 hash in Firebase under:
     * - users/<deviceId>/passphraseHash
     * - passIndex/<hash> -> <deviceId>
     */
    suspend fun generatePassphraseForDeviceId(context: Context, deviceId: String): String = withContext(Dispatchers.IO) {
        val pass = buildPassphrase()
        val hash = HashUtils.sha256(pass)
        try {
            val ref = FirebaseDatabase.getInstance().reference
            val updates = hashMapOf<String, Any>(
                "users/$deviceId/passphraseHash" to hash,
                "users/$deviceId/passphraseUpdatedAt" to System.currentTimeMillis(),
                "passIndex/$hash" to deviceId
            )
            ref.updateChildren(updates).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save passphrase hash: ${e.message}")
        }
        return@withContext pass
    }

    fun generateQRCodeForDeviceId(deviceId: String): Bitmap {
        // Encode deep link with deviceId
        val payload = "selfnote://login?deviceId=$deviceId"
        return QRUtils.generateQrBitmap(payload)
    }

    private fun buildPassphrase(): String {
        val alphabet = ("ABCDEFGHJKLMNPQRSTUVWXYZ" + "23456789").toCharArray() // avoid confusing chars
        val rnd = java.security.SecureRandom()
        val parts = (1..3).map {
            (1..4).map { alphabet[rnd.nextInt(alphabet.size)] }.joinToString("")
        }
        return parts.joinToString("-")
    }

    suspend fun migrateAnonymousDataToGoogleAccount(context: Context, googleUid: String, deviceId: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            val ref = FirebaseDatabase.getInstance().reference
            val updates = hashMapOf<String, Any>(
                "users/$deviceId/googleUid" to googleUid,
                "users/$deviceId/migratedAt" to System.currentTimeMillis()
            )
            ref.updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
