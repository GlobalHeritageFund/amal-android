package amal.global.amal

import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.*

data class ReportDraft internal constructor(
        //had to change id to var to allow id to be reset when re-create draft from preferences
        var id: String = UUID.randomUUID().toString(),
        var images: List<LocalImage> = listOf(),
        var deviceToken: String = "",
        var creationDate: Date = Date(),
        var title: String = "",
        var assessorEmail: String = "",
        //as of 11/15/22 var uploadToEAMANA has been removed and is not set for new reports
        //restTarget introduced 11/15/22 if stays null know to save to firebase
        var restTarget: RestTarget? = null,
) {

    companion object {
        @JsonClass(generateAdapter = true)
        data class DraftWrapper(@Json(name = "list") val list: MutableList<ReportDraft>)

        val jsonAdapter: JsonAdapter<DraftWrapper>
            get() {
                val moshi = Moshi.Builder()
                        .add(DateAdapter())
                        .add(KotlinJsonAdapterFactory())
                        .build()
                return moshi.adapter(DraftWrapper::class.java)
            }
    }
}

class DateAdapter {
    @ToJson
    fun toJson(date: Date?): Long? {
        return date?.time
    }

    @FromJson
    fun fromJson(value: Long?): Date? {
        return value?.let { Date(it) }
    }
}


