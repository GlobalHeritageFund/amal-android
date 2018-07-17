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

class ImageImporter(val activity: Activity, val requestCode: Int, val intent: Intent?) {

    companion object {
        const val imageImportRequestCode = 123
    }

    private val isValid: Boolean
        get() = requestCode == ImageImporter.imageImportRequestCode

    private val imageUri = intent?.data ?: throw InvalidParameterException("imageURI should be non-null.")

    private val inputStream: InputStream
        get() = activity.contentResolver.openInputStream(imageUri)

    private fun buildMetadata(): Metadata {
        val metadata = Metadata()
        val exifInterface = ExifInterface(inputStream)
        val doubleArray = exifInterface.latLong ?: doubleArrayOf()
        if (doubleArray.count() == 2) {
            metadata.latitude = doubleArray[0]
            metadata.longitude = doubleArray[1]
        }
        metadata.date = exifInterface.getTimeStamp()
        return metadata
    }

    fun importImage(): Boolean {
        if (!isValid) { return false }
        return try {
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            val byteArray = outputStream.toByteArray()
            PhotoStorage(activity).savePhotoLocally(byteArray, buildMetadata())
            true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            false
        }

    }
}
