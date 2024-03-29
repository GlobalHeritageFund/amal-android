package amal.global.amal

import android.content.Context
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.io.File
import java.util.*

interface Image {
    fun load(context: Context) : RequestBuilder<Drawable>
    var metadata: Metadata
}

data class LocalImage internal constructor(
        var filePath: String,
        var settingsPath: String,
        override var metadata: Metadata
) : Image {
    companion object {
        fun convenience(filePath: String, settingsPath: String) : LocalImage {
            val metadata = try {
                Metadata.jsonAdapter.fromJson(File(settingsPath).readText())
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            return LocalImage(filePath, settingsPath, metadata ?: Metadata())
        }
    }

    fun saveMetaData() {
        val json = Metadata.jsonAdapter.toJson(metadata)
        File(settingsPath).writeText(json)
    }

    val file: File by lazy {
        File(this.filePath)
    }

    val date: Date
        get() = metadata.dateValue ?: Date(File(filePath).lastModified())

    val localDateString: String?
        get() {
            val fmt: DateTimeFormatter = DateTimeFormat.forPattern("MMMM d, yyyy")
            val localDate = metadata.localDateValue ?: DateTime(File(filePath).lastModified()).toLocalDate()
            return localDate.toString(fmt)
        }

    override fun load(context: Context): RequestBuilder<Drawable> {
        return Glide.with(context).load(file)
    }
}

@JsonClass(generateAdapter = true)
data class RemoteImage(
        @Json(name = "imageRef") val remoteStorageLocation: String,
        @Json(name = "settings") override var metadata: Metadata
) : Image {
    companion object {
        val jsonAdapter: JsonAdapter<RemoteImage>
            get() {
                val moshi = Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()
                return moshi.adapter(RemoteImage::class.java)
            }
    }

    val firebaseReference: StorageReference
        get() = FirebaseStorage.getInstance().getReference(remoteStorageLocation)

    override fun load(context: Context): RequestBuilder<Drawable> {
        return Glide.with(context).load(firebaseReference)
    }
}