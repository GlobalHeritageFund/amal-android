package global.amal.app

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.security.InvalidParameterException
import java.text.SimpleDateFormat
import java.util.Locale


class ImageImporter(private val activity: Activity, val onComplete: () -> Unit): IntentRequest {

    companion object {
        const val TAG = "ImageImporter"
    }

    override val requestCode = 23632

    override fun start() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        activity.startActivityForResult(intent, requestCode)
    }

    override fun finalize(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
        // If the user cancelled, resultCode will be Activity.RESULT_CANCELED (which is 0).
        if (resultCode != Activity.RESULT_OK) {
            Log.d(TAG, "Image selection cancelled by user or system failure. Result code: $resultCode")
            onComplete()
            return true
        }


        val imageUri = intent?.data
        if (imageUri == null) {
            Log.e(TAG, "Image selection returned RESULT_OK but imageURI is null.")
            // Handle as an unexpected failure, but still complete the request.
            onComplete()
            return false // Indicate failure to import, but request completed.
        }

        fun buildMetadata(): Metadata {
            val metadata = Metadata()
            val inputStream = activity.contentResolver.openInputStream(imageUri)
            if (inputStream != null) {
                try {
                    val exifInterface = ExifInterface(inputStream)
                    val (latitude, longitude) = getLatLongAPI29andAbove(exifInterface)
                    val photoDate = exifInterface.getAttribute(ExifInterface.TAG_DATETIME)
                    if (photoDate != null) {
                        val parsedDate = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US).parse(photoDate)
                        metadata.date = parsedDate?.time
                    }
                    if (latitude != null && longitude != null) {
                        metadata.latitude = latitude
                        metadata.longitude = longitude
                    } else {
                        println("Could not retrieve latitude and longitude from image.")
                    }
                } catch (e: IOException) {
                    println("Error reading EXIF data: ${e.message}")
                }
// replaced the below lines with the lines above 6/2/25 because time wasn't being set correctly
//and lat long were not being fetched
//think issue was api, and current code works for API 29+ with the associated permissions
//                val doubleArray = exifInterface.latLong ?: doubleArrayOf()
//                if (doubleArray.count() == 2) {
//                    metadata.latitude = doubleArray[0]
//                    metadata.longitude = doubleArray[1]
//                }

//                metadata.date = exifInterface.getTimeStamp()?.time
            }
            return metadata
        }

        return try {
            val inputStream = activity.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val byteArray = outputStream.toByteArray()
            PhotoStorage(activity.applicationContext).saveExternalPhotoLocally(byteArray, buildMetadata())
            onComplete()
            true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            onComplete()
            false
        }
    }

    private fun getLatLongAPI29andAbove(exif: ExifInterface): Pair<Double?, Double?> {
        val latValue = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
        val latRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF)
        val lonValue = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
        val lonRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF)

        val latitude = convertDMSLatitude(latValue, latRef)
        val longitude = convertDMSLongitude(lonValue, lonRef)

        return Pair(latitude, longitude)
    }

    private fun convertDMSLatitude(latValue: String?, latRef: String?): Double? {
        if (latValue == null || latRef == null) return null

        val parts = latValue.split(",")
        if (parts.size != 3) return null

        val degreeParts = parts[0].split("/").map { it.trim().toIntOrNull() ?: return null }
        val minuteParts = parts[1].split("/").map { it.trim().toIntOrNull() ?: return null }
        val secondParts = parts[2].split("/").map { it.trim().toIntOrNull() ?: return null }
        if (degreeParts.size != 2 || minuteParts.size != 2 || secondParts.size != 2) return null
        val degrees = degreeParts[0].toDouble() / degreeParts[1]
        val minutes = minuteParts[0].toDouble() / minuteParts[1]
        val seconds = secondParts[0].toDouble() / secondParts[1]

        var decimalDegrees = degrees + minutes / 60 + seconds / 3600

        if (latRef == "S") {
            decimalDegrees = -decimalDegrees
        }

        return decimalDegrees
    }

    private fun convertDMSLongitude(lonValue: String?, lonRef: String?): Double? {
        if (lonValue == null || lonRef == null) return null

//        val parts = lonValue.split(",").map { it.trim().toDoubleOrNull() ?: return null }
        val parts = lonValue.split(",")
        if (parts.size != 3) return null

        val degreeParts = parts[0].split("/").map { it.trim().toIntOrNull() ?: return null }
        val minuteParts = parts[1].split("/").map { it.trim().toIntOrNull() ?: return null }
        val secondParts = parts[2].split("/").map { it.trim().toIntOrNull() ?: return null }
        if (degreeParts.size != 2 || minuteParts.size != 2 || secondParts.size != 2) return null
        val degrees = degreeParts[0].toDouble() / degreeParts[1]
        val minutes = minuteParts[0].toDouble() / minuteParts[1]
        val seconds = secondParts[0].toDouble() / secondParts[1]

        var decimalDegrees = degrees + minutes / 60 + seconds / 3600

        if (lonRef == "W") {
            decimalDegrees = -decimalDegrees
        }

        return decimalDegrees
    }
}
