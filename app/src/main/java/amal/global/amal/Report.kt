package amal.global.amal

import java.util.*
import kotlin.collections.HashMap

data class Report internal constructor(
        val images: List<Image>,
        val deviceToken: String,
        val creationDate: Date,
        val title: String,
        val assessorEmail: String?
) {

    companion object {
        fun fromJSON(map: HashMap<String, Any>): Report? {
            val deviceToken = map["authorDeviceToken"] as? String ?: return null
            val creationDate = map["creationDate"] as? Double ?: return null
            val title = map["title"] as? String ?: return null
            val assessorEmail = map["assessorEmail"] as? String

            return Report(listOf(), deviceToken, Date((creationDate*1000).toLong()), title, assessorEmail)
        }
    }
}