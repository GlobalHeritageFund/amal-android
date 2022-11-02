package amal.global.amal

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class PhotoStorage internal constructor(internal var context: Context) {

    @Throws(IOException::class)
    public fun savePhotoLocally(bytes: ByteArray, metadata: Metadata) {
        val dir = File(context.filesDir.toString() + "/images/")
        dir.mkdirs()

        val maxImageID = dir.listFiles()
                .mapNotNull { file ->  file.nameWithoutExtension.toIntOrNull() }
                .maxOrNull() ?: 0

        val newImageID = maxImageID + 1
        val imageFile = File(dir.toString() + "/$newImageID.jpg")
        val settingsFile = File(dir.toString() + "/$newImageID.json")
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

    public fun deleteImage(imagePath: String, settingsPath: String) {
        Log.d("PhotoStorage","delete image was called")
        try {
            //delete photo jpeg file
            val imageFile = File(imagePath)
            imageFile.delete()
            //delete affiliated json file - no idea if this will work - just trying easy first
            val metaDataFile = File(settingsPath)
            metaDataFile.delete()
        } catch(e:Exception) {
           Log.e(e.toString(),"Error deleting photo")
        }
            //TODO should probably add some error protection at some point - also may eventually add to WorkManager
    }

    public fun fetchImages(): List<LocalImage> {
        val dir = File(context.filesDir.toString() + "/images/")
        dir.mkdirs()
        return dir
                .listFiles()
                .filter { file ->
                    file.extension == "jpg" || file.extension == "jpeg"
                }
                .map { file ->
                    LocalImage.convenience(file.absolutePath, file.absolutePath.replaceAfterLast(".", "json"))
                }
                .sortedBy { image ->
                    image.date
                }
                .reversed()
    }

}
