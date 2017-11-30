package amal.global.amal

import java.util.*

data class Report internal constructor(
        val images: List<Image>,
        val localIdentifier: String,
        val creationDate: Date,
        val title: String,
        val assessorEmail: String
)
