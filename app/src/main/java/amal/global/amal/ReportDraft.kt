package amal.global.amal

import java.util.*

data class ReportDraft internal constructor(
        var images: List<LocalImage> = listOf(),
        var deviceToken: String = "",
        var creationDate: Date = Date(),
        var title: String = "",
        var assessorEmail: String = "",
        var uploadToEAMENA: Boolean = false
)
