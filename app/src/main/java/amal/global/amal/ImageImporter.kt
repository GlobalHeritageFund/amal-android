package amal.global.amal

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.security.InvalidParameterException

class ImageImporter(val activity: Activity, val requestCode: Int, val intent: Intent?) {

    companion object {
        const val imageImportRequestCode = 123
    }

    private val isValid: Boolean
        get() = requestCode == ImageImporter.imageImportRequestCode

    private val imageUri = intent?.data ?: throw InvalidParameterException("imageURI should be non-null.")

    val metadata: Metadata
        get() = Metadata()

    fun importImage(): Boolean {
        if (!isValid) { return false }
        return try {
            val stream = activity.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(stream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val byteArray = outputStream.toByteArray()
            PhotoStorage(activity).savePhotoLocally(byteArray, metadata)
            true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            false
        }

    }
}