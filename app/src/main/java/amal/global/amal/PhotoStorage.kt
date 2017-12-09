package amal.global.amal

import android.content.Context

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.ArrayList


class PhotoStorage internal constructor(internal var context: Context) {

    @Throws(IOException::class)
    public fun savePhotoLocally(bytes: ByteArray) {
        val dir = File(context.filesDir.toString() + "/images/")
        dir.mkdirs()

        val maxImageID = dir.listFiles()
                .mapNotNull { file ->  file.nameWithoutExtension.toIntOrNull() }
                .max() ?: 0

        val newImageID = maxImageID + 1
        val file = File(dir.toString() + "/$newImageID.jpg")
        try {
            save(bytes, file)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

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

    public fun fetchImages(): List<Image> {
        val dir = File(context.filesDir.toString() + "/images/")
        dir.mkdirs()
        return dir
                .listFiles()
                .filter { file ->
                    file.extension == "jpg" || file.extension == "jpeg"
                }
                .map { file ->
                    Image.convenience(file.absolutePath, file.absolutePath.replaceAfterLast(".", "json"))
                }

    }

}
