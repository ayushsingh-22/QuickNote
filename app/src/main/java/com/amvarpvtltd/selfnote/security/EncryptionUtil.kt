package com.amvarpvtltd.selfnote.security

import android.util.Base64
import android.util.Log
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest

object EncryptionUtil {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/ECB/PKCS5Padding"
    private const val TAG = "EncryptionUtil"

    // Generate a secret key based on device ID for consistency
    private fun generateKey(deviceId: String): SecretKey {
        val actualDeviceId = deviceId.ifEmpty {
            Log.w(TAG, "Device ID is empty, using fallback")
            "DefaultDeviceId123"
        }

        Log.d(TAG, "Generating key for device ID: $actualDeviceId")

        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(actualDeviceId.toByteArray())
        val keyBytes = hash.copyOf(16)

        return SecretKeySpec(keyBytes, ALGORITHM)
    }

    /**
     * Encrypts the given text using AES encryption
     */
    fun encrypt(plainText: String, deviceId: String): String {
        return try {
            if (plainText.isEmpty()) return plainText

            // Don't encrypt if it's already encrypted
            if (isEncrypted(plainText)) {
                Log.d(TAG, "Text is already encrypted, returning as is")
                return plainText
            }

            Log.d(TAG, "Encrypting text of length: ${plainText.length}")
            val key = generateKey(deviceId)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)

            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val encrypted = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
            Log.d(TAG, "Encryption successful, result length: ${encrypted.length}")
            encrypted
        } catch (e: Exception) {
            Log.e(TAG, "Encryption failed", e)
            plainText // Return original text if encryption fails
        }
    }

    fun decrypt(encryptedText: String, deviceId: String): String {
        return try {
            if (encryptedText.isEmpty()) return encryptedText

            // Check if the text is actually encrypted
            if (!isEncrypted(encryptedText)) {
                Log.d(TAG, "Text doesn't appear to be encrypted, returning as is")
                return encryptedText
            }

            Log.d(TAG, "Attempting to decrypt text of length: ${encryptedText.length}")
            val key = generateKey(deviceId)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key)

            val encryptedBytes = Base64.decode(encryptedText, Base64.NO_WRAP)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            val decrypted = String(decryptedBytes, Charsets.UTF_8)
            Log.d(TAG, "Decryption successful, result length: ${decrypted.length}")
            decrypted
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed for text: ${encryptedText.take(20)}...", e)
            // Return the original text if it's not actually encrypted
            encryptedText
        }
    }

    fun isEncrypted(text: String): Boolean {
        return try {
            // More lenient check for encrypted content
            if (text.length < 8) return false
            if (text.contains(" ") || text.contains("\n")) return false

            // Try to decode as Base64
            val decoded = Base64.decode(text, Base64.NO_WRAP)
            decoded.isNotEmpty() && text.matches(Regex("^[A-Za-z0-9+/]*={0,2}$"))
        } catch (e: Exception) {
            false
        }
    }
}
