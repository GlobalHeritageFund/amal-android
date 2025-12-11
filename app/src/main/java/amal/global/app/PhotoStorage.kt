package global.amal.app

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class PhotoStorage internal constructor(internal var context: Context) {

    companion object {
        const val TAG = "PhotoStorage"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    @Throws(IOException::class)
    //before was savePhotoLocally and called from both ImageImporter and CaptureFragment
    //now called from ImageImporter only as CaptureFragment now has its own logic
    //renamed this fun to saveExternalPhotoLocally to avoid confusion
    fun saveExternalPhotoLocally(bytes: ByteArray, metadata: Metadata) {
        val dir = File(context.filesDir.toString() + "/images/")
        dir.mkdirs()
        //todo also better error handling for this and success toast
        val timeInMillis = metadata.date ?: System.currentTimeMillis()
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(timeInMillis)
        val imageFile = File("$dir/$name.jpg")
        val settingsFile = File("$dir/$name.json")
        try {
            save(bytes, imageFile)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        settingsFile.writeText(Metadata.jsonAdapter.toJson(metadata))
    }

    @Throws(IOException::class)
    private fun save(bytes: ByteArray, file: File) {
        var output: OutputStream? = null
        try {
            output = FileOutputStream(file)
            output.write(bytes)
        } finally {
            output?.close()
        }
    }

    fun deleteImage(imagePath: String, settingsPath: String) {
        Log.d(TAG,"delete image was called")
        try {
            //delete photo jpeg file
            val imageFile = File(imagePath)
            imageFile.delete()
            //delete affiliated json file - no idea if this will work - just trying easy first
            val metaDataFile = File(settingsPath)
            metaDataFile.delete()
        } catch(e: Exception) {
           Log.e(TAG, "Error deleting photo", e)
        }
            //TODO should probably add some error protection at some point - also may eventually add to WorkManager
    }

    fun fetchImagesSortedByDateDesc(): List<LocalImage> {
        val dir = File(context.filesDir.toString() + "/images/")
        dir.mkdirs()
        val files = dir.listFiles()?.toList().orEmpty()
        return files
            .asSequence()
            .filter { it.extension.equals("jpg", true) || it.extension.equals("jpeg", true) }
            .map { file ->
                LocalImage.convenience(
                    file.absolutePath,
                    file.absolutePath.replaceAfterLast(".", "json")
                )
            }
            .sortedByDescending { image -> image.date }
            .toList()
    }
}
