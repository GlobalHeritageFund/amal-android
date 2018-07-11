package amal.global.amal

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.HashMap

data class Report internal constructor(
        val firebaseID: String,
        val images: List<RemoteImage>,
        val deviceToken: String,
        val creationDate: Date,
        val title: String,
        val assessorEmail: String?
) {

    val webURL: Uri
        get() = Uri.parse("https://app.amal.global/reports/" + this.firebaseID)

    fun fetchPDFURL(): Promise<Uri> {
        return Promise<Uri>({ fulfill, reject ->
            val reference = FirebaseStorage.getInstance().reference
                    .child("pdfs")
                    .child(firebaseID + ".pdf")
            val task = reference.downloadUrl

            task.addOnCompleteListener({ task ->
                val exception = task.exception
                if (task.isSuccessful) {
                    fulfill(task.result)
                } else if (exception != null) {
                    reject(Error(exception.message))
                }
            })

            task.addOnFailureListener({ exception ->
                reject(Error(exception.message))
            })

        })
    }

    companion object {
        fun fromJSON(id: String, map: HashMap<String, Any>): Report? {
            val uploadComplete = (map["uploadComplete"] as? Boolean) ?: false
            if (!uploadComplete) { return null }
            val deviceToken = map["authorDeviceToken"] as? String ?: return null
            val creationDate = map["creationDate"] as? Double ?: return null
            val title = map["title"] as? String ?: return null
            val assessorEmail = map["assessorEmail"] as? String

            val imagesMaps = map["images"] as? HashMap<*, *> ?: hashMapOf<String, Any>()
            val images = imagesMaps
                    .values
                    .filterIsInstance<HashMap<String, Any>>()
                    .mapNotNull { RemoteImage.fromJSON(it) }
            return Report(id, images, deviceToken, Date((creationDate*1000).toLong()), title, assessorEmail)
        }
    }
}
