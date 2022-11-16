package amal.global.amal

import java.util.*

data class ReportDraft internal constructor(
        var images: List<LocalImage> = listOf(),
        var deviceToken: String = "",
        var creationDate: Date = Date(),
        var title: String = "",
        var assessorEmail: String = "",
        //as of 11/15/22 uploadToEAMANA is not set for new reports so will be false for all
        var uploadToEAMENA: Boolean = false,
        //restTarget introduced 11/15/22 if stays null know to save to firebase
        var restTarget: RestTargets? = null
)
