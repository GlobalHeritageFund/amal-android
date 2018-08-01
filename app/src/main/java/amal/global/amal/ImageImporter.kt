package amal.global.amal

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.security.InvalidParameterException

class ImageImporter(val activity: Activity, val onComplete: () -> Unit): IntentRequest {

    override val requestCode: Int
        get() = 23632

    override fun start() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        activity.startActivityForResult(intent, requestCode)
    }

    override fun finalize(requestCode: Int, intent: Intent?): Boolean {
        val imageUri = intent?.data ?: throw InvalidParameterException("imageURI should be non-null.")

        fun buildMetadata(): Metadata {
            val metadata = Metadata()
            val inputStream = activity.contentResolver.openInputStream(imageUri)
            val exifInterface = ExifInterface(inputStream)
            val doubleArray = exifInterface.latLong ?: doubleArrayOf()
            if (doubleArray.count() == 2) {
                metadata.latitude = doubleArray[0]
                metadata.longitude = doubleArray[1]
            }
            metadata.date = exifInterface.getTimeStamp()
            return metadata
        }

        return try {
            val inputStream = activity.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val byteArray = outputStream.toByteArray()
            PhotoStorage(activity).savePhotoLocally(byteArray, buildMetadata())
            onComplete()
            true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            false
        }
    }
}
