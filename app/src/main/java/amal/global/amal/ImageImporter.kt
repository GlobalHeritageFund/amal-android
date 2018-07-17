package amal.global.amal

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

class ImageImporter {
    companion object {
        val imageImportRequestCode = 123
    }

    fun importImage(activity: Activity, requestCode: Int, intent: Intent?): Boolean {
        if (requestCode != ImageImporter.imageImportRequestCode) { return false }
        val imageUri = intent?.data ?: return false
        return try {
            val stream = activity.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(stream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val byteArray = outputStream.toByteArray()
            val metadata = Metadata()
            PhotoStorage(activity).savePhotoLocally(byteArray, metadata)
            true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            false
        }

    }
}