package amal.global.amal

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.*
import kotlin.collections.HashMap

interface ReportInterface {

}

@JsonClass(generateAdapter = true)
data class Report internal constructor(
        val firebaseID: String,
        val images: List<RemoteImage> = listOf(),
        @Json(name="authorDeviceToken") val deviceToken: String,
        val creationDate: Double,
        val title: String,
        val assessorEmail: String?,
        var uploadComplete: Boolean
) : ReportInterface {

    val webURL: Uri
        get() = Uri.parse("https://app.amal.global/reports/" + this.firebaseID)

    fun fetchPDFURL(): Promise<Uri> {
        return Promise<Uri> { fulfill, reject ->
            val reference = FirebaseStorage.getInstance().reference
                    .child("pdfs")
                    .child(firebaseID + ".pdf")
            val task = reference.downloadUrl

            task.addOnCompleteListener { task ->
                val exception = task.exception
                if (task.isSuccessful) {
                    fulfill(task.result)
                } else if (exception != null) {
                    reject(Error(exception.message))
                }
            }

            task.addOnFailureListener { exception ->
                reject(Error(exception.message))
            }

        }
    }

    val creationDateValue: Date
        get() = Date((creationDate * 1000).toLong())

    companion object {
        val jsonAdapter: JsonAdapter<Report>
            get() {
                val moshi = Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()
                return moshi.adapter(Report::class.java)
            }

    }
}
