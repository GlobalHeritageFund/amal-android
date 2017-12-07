package amal.global.amal

import java.util.*

data class Report internal constructor(
        val images: List<Image>,
        val deviceToken: String,
        val creationDate: Date,
        val title: String,
        val assessorEmail: String
)
