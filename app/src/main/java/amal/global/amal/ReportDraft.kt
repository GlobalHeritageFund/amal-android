package amal.global.amal

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.sql.Wrapper
import java.util.*

@Entity(tableName = "report_draft_table")
data class ReportDraft internal constructor(
        @PrimaryKey val id: String = UUID.randomUUID().toString(),
        var images: List<LocalImage> = listOf(),
        var deviceToken: String = "",
        var creationDate: Date = Date(),
        var title: String = "",
        var assessorEmail: String = "",
        //as of 11/15/22 uploadToEAMANA is not set for new reports so will be false for all
        //TODO make sure uploadToEAMANA is never used and then stop setting it
        var uploadToEAMENA: Boolean = false,
        //restTarget introduced 11/15/22 if stays null know to save to firebase
        var restTarget: RestTarget? = null,
        //uploadStatus introduced 11/17/22 but will not be used unless implement workmanager
        var upLoadStatus: String? = null
) {

    companion object {
//        this will be used for upload status when implement work manager
//        const val QUEUED = "Queued for upload"
//        const val DRAFT = "Draft"
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

class Converters {
    private val moshi = Moshi.Builder().build()
    private val localImageType = Types.newParameterizedType(List::class.java, LocalImage::class.java)
    private val imagesAdapter = moshi.adapter<List<LocalImage>>(localImageType)
    private val restTargetType = Types.newParameterizedType(RestTarget::class.java)
    private val restTargetAdapter = moshi.adapter<RestTarget>(restTargetType)

    @TypeConverter
    fun stringToImages(string: String): List<LocalImage> {
        return imagesAdapter.fromJson(string).orEmpty()
    }

    @TypeConverter
    fun imagesToString(images: List<LocalImage>): String {
        return imagesAdapter.toJson(images)
    }

    @TypeConverter
    fun stringToRestTarget(string: String): RestTarget? {
        return restTargetAdapter.fromJson(string)
    }

    @TypeConverter
    fun restTargetToString(restTarget: RestTarget): String {
        return restTargetAdapter.toJson(restTarget)
    }

    @TypeConverter
    fun timestampToDate(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }
}

