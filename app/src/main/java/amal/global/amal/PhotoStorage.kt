package amal.global.amal

import android.content.Context

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.ArrayList


class PhotoStorage internal constructor(internal var context: Context) {

    public fun savePhotoLocally(bytes: ByteArray) {
        val dir = File(context.filesDir.toString() + "/images/")
        dir.mkdirs()
        var maxImageID = -1
        for (file in dir.listFiles()) {
            val filename = file.name
            val IDString = filename.removeExtension()
            val id = Integer.parseInt(IDString)
            if (id > maxImageID) {
                maxImageID = id
            }
        }
        val newImageID = maxImageID + 1
        val file = File(dir.toString() + "/$newImageID.jpg")
        val output: OutputStream? = null
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
            if (null != output) {
                output.close()
            }
        }
    }

    public fun fetchImages(): ArrayList<Image> {
        val dir = File(context.filesDir.toString() + "/images/")
        dir.mkdirs()
        val files = dir.listFiles()
        val images = ArrayList<Image>()
        for (file in files) {
            val filename = file.name
            val fileExtension = filename.getExtension()
            val IDString = filename.removeExtension()
            if (fileExtension == "jpg" || fileExtension == "jpeg") {
                images.add(Image(file.absolutePath))
            }
        }
        return images
    }

}
