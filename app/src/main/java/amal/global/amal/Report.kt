package amal.global.amal

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

    companion object {
        fun fromJSON(id: String, map: HashMap<String, Any>): Report? {
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
