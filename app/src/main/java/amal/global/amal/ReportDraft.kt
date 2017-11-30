package amal.global.amal

import java.util.*

data class ReportDraft internal constructor(
        var images: List<Image> = listOf(),
        var localIdentifier: String = "",
        var creationDate: Date = Date(),
        var title: String = "",
        var assessorEmail: String = ""
)
