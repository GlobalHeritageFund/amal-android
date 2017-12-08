package amal.global.amal

import java.util.*
import kotlin.collections.HashMap

data class Report internal constructor(
        val images: List<Image>,
        val deviceToken: String,
        val creationDate: Date,
        val title: String,
        val assessorEmail: String
) {

    companion object {
        fun fromJSON(map: HashMap<String, Any>): Report? {
            val deviceToken = map["authorDeviceToken"] as String
            val creationDate = Date()
            val title = map["title"] as String
            val assessorEmail = ""// map["assessorEmail"] as String

            return Report(listOf(), deviceToken, creationDate, title, assessorEmail)
        }
    }
}
