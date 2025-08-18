import java.util.UUID
import com.amvarpvtltd.selfnote.security.EncryptionUtil
import android.util.Log


var myGlobalMobileDeviceId: String = ""


data class dataclass(
    val title: String = "",
    val description: String = "",
    var id: String = UUID.randomUUID().toString(),
    var mymobiledeviceid: String =  myGlobalMobileDeviceId
) {
    // Encrypted versions for Firebase storage
    fun getEncryptedTitle(): String {
        return EncryptionUtil.encrypt(title, mymobiledeviceid)
    }

    fun getEncryptedDescription(): String {
        return EncryptionUtil.encrypt(description, mymobiledeviceid)
    }

    // Create encrypted version for Firebase
    fun toEncryptedData(): dataclass {
        return dataclass(
            title = getEncryptedTitle(),
            description = getEncryptedDescription(),
            id = id,
            mymobiledeviceid = mymobiledeviceid
        )
    }

    companion object {
        private const val TAG = "dataclass"

        // Create dataclass from encrypted Firebase data
        fun fromEncryptedData(encryptedData: dataclass): dataclass {
            return try {
                Log.d(TAG, "Decrypting note with ID: ${encryptedData.id}")
                Log.d(TAG, "Device ID for decryption: ${encryptedData.mymobiledeviceid}")

                val decryptedTitle = EncryptionUtil.decrypt(encryptedData.title, encryptedData.mymobiledeviceid)
                val decryptedDescription = EncryptionUtil.decrypt(encryptedData.description, encryptedData.mymobiledeviceid)

                Log.d(TAG, "Decryption completed for note: ${encryptedData.id}")

                dataclass(
                    title = decryptedTitle,
                    description = decryptedDescription,
                    id = encryptedData.id,
                    mymobiledeviceid = encryptedData.mymobiledeviceid
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error in fromEncryptedData", e)
                // Return the original data - it might not be encrypted
                encryptedData
            }
        }
    }
}
