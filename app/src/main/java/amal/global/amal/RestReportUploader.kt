package amal.global.amal

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import okhttp3.RequestBody
import okhttp3.MultipartBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class RestReportUploader(val reportDraft: ReportDraft) {

    private val client = OkHttpClient()

    val promise = Promise<Report>()

    private val jpegContentType = MediaType.parse("image/jpeg")

    private val jsonContentType = MediaType.parse("application/json; charset=utf-8")

    fun upload() {
        val imageUploads = reportDraft.images.map { uploadImage(it) }.asSequence()

        Promise.all(imageUploads)
                .flatMap {
                    return@flatMap postReport(it) //pass in parsed images
                }
                .then {
                    promise.fulfill(it)
                }
                .catch {
                    promise.reject(it)
                }
    }

    private fun postReport(images: List<HerBridgeImage>): Promise<Report> {

        val assessorJSON = JSONObject()
        assessorJSON.put("email", reportDraft.assessorEmail)
        assessorJSON.put("name", "")
        assessorJSON.put("deviceToken", reportDraft.deviceToken)

        val zipped = images.zip(reportDraft.images)
        val resources = zipped
                .map { pair ->
                    val herBridgeImage = pair.first
                    val localImage = pair.second
                    val json = JSONObject()
                    json.put("type", localImage.metadata.type)
                    json.put("name", localImage.metadata.name)
                    json.put("notes", localImage.metadata.notes)
                    json.put("images", JSONArray(arrayOf(herBridgeImage.id)))
                    json.put("condition", localImage.metadata.condition)
                    json.put("hazards", localImage.metadata.hazards)
                    json.put("safetyHazards", localImage.metadata.safetyHazards)
                    json.put("interventionRequired", localImage.metadata.interventionRequired)
                    json
                }

        val json = JSONObject()
        json.put("title", reportDraft.title)
        json.put("assessor", assessorJSON)
        json.put("createdAt", Date().time/1000)
        json.put("resources", JSONArray(resources))
        json.put("type", "field_report")

        val request = Request.Builder()
                .url("http://herbridge.legiongis.com/api/reports/")
                .post(RequestBody.create(jsonContentType, json.toString()))
                .build()

        return client.newCall(request).enqueue()
                .map {
                    Report.jsonAdapter.fromJson(it.body()?.string() ?: "")!!
                }
    }

    private fun uploadImage(image: LocalImage): Promise<HerBridgeImage> {

        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("latitude", image.metadata.latitude.toString())
                .addFormDataPart("longitude", image.metadata.longitude.toString())
                .addFormDataPart("caption", image.metadata.name)
                .addFormDataPart("captureDate", image.metadata.date?.toString() ?: "0")
                .addFormDataPart("image", "image_1.jpg", RequestBody.create(jpegContentType, image.file))
                .build()

        val request = Request.Builder()
                .url("http://herbridge.legiongis.com/api/images/")
                .method("POST", requestBody)
                .build()

        return client
                .newCall(request)
                .enqueue()
                .map {
                    val responseString = it.body()?.string() ?: ""
                    HerBridgeImage.jsonAdapter.fromJson(responseString)!!
                }
    }

}

@JsonClass(generateAdapter = true)
data class HerBridgeImage(
        val id: String,
        val url: String,
        val thumbnailUrl: String,
        val latitude: Double,
        val longitude: Double,
        val captureDate: Double,
        val caption: String
        ) {
    companion object {
        val jsonAdapter: JsonAdapter<HerBridgeImage>
            get() {
                val moshi = Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()
                return moshi.adapter(HerBridgeImage::class.java)
            }
    }
}