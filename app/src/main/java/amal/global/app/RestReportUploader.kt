package global.amal.app

import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.*

class RestReportUploader(private val reportDraft: ReportDraft) {

//    private var baseURL = reportDraft.restTarget!!.url
    private val baseUrl = requireNotNull(reportDraft.restTarget?.url) {
     "RestReportUploader: restTarget is null; cannot upload via REST." }.toHttpUrl()
    private val client = OkHttpClient()
    private val jpegContentType = "image/jpeg".toMediaTypeOrNull()
    private val jsonContentType = "application/json; charset=utf-8".toMediaTypeOrNull()
    val promise = Promise<ReportInterface>()

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

    private fun postReport(images: List<HerBridgeImage>): Promise<ReportInterface> {

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

//        val request = Request.Builder()
//                .url("${baseURL.toHttpUrl()}api/reports/")
//                .post(json.toString().toRequestBody(jsonContentType))
//                .build()

        val reportsUrl = baseUrl.newBuilder()
            .addPathSegment("api")
            .addPathSegment("reports")
            .addPathSegment("")//to get backslash at end

            .build()
        Log.d("RestReportUploader",reportsUrl.toString())

        val request = Request.Builder()
            .url(reportsUrl)
            .post(json.toString().toRequestBody(jsonContentType))
            .build()

        return client.newCall(request).enqueue()
            .map { response -> // okhttp3.Response
                val responseBodyString = response.body?.string()
                if (response.isSuccessful && !responseBodyString.isNullOrBlank()) {
                    HerBridgeReport.jsonAdapter.fromJson(responseBodyString)
                        ?: throw IOException("Failed to parse HerBridgeReport JSON. Body: $responseBodyString")
                } else {
                    throw IOException("REST API Error for postReport. Code: ${response.code}. Message: ${response.message}. Body: $responseBodyString")
                }
            }
    }

    private fun uploadImage(image: LocalImage): Promise<HerBridgeImage> {

        val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("latitude", image.metadata.latitude.toString())
                .addFormDataPart("longitude", image.metadata.longitude.toString())
                .addFormDataPart("caption", image.metadata.name)
                .addFormDataPart("captureDate", (image.date.time /1000).toString())
                .addFormDataPart("image", "image_1.jpg", image.file.asRequestBody(jpegContentType))
                .build()

        val imagesUrl = baseUrl.newBuilder()
            .addPathSegment("api")
            .addPathSegment("images")
            .addPathSegment("")
            .build()
        Log.d("RestReportUploader",imagesUrl.toString())

        val request = Request.Builder()
            .url(imagesUrl)
            .post(requestBody)
            .build()

        return client.newCall(request).enqueue()
            .map { response ->
                val responseBodyString = response.body?.string()
                if (response.isSuccessful && !responseBodyString.isNullOrBlank()) {
                    HerBridgeImage.jsonAdapter.fromJson(responseBodyString)
                        ?: throw IOException("Failed to parse HerBridgeImage JSON. Body: $responseBodyString")
                } else {
                    throw IOException("REST API Error for uploadImage. Code: ${response.code}. Message: ${response.message}. Body: $responseBodyString")
                }
            }
    }

}

@JsonClass(generateAdapter = true)
data class HerBridgeReport(val title: String) : ReportInterface {
    companion object {
        val jsonAdapter: JsonAdapter<HerBridgeReport>
            get() {
                val moshi = Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()
                return moshi.adapter(HerBridgeReport::class.java)
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