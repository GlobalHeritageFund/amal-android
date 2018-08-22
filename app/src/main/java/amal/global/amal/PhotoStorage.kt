package amal.global.amal

import android.content.Context
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
                .max() ?: 0

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
