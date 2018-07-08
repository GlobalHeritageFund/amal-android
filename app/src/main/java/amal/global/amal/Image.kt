package amal.global.amal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.storage.StorageManager
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import kotlin.collections.HashMap

interface Image {
    fun loadThumbnail(context: Context) : Promise<Bitmap>
    fun loadFullSize(context: Context) : Promise<Bitmap>
    var metadata: Metadata
}

data class LocalImage internal constructor(
        var filePath: String,
        var settingsPath: String,
        override var metadata: Metadata
) : Image {
    companion object {
        fun convenience(filePath: String, settingsPath: String) : LocalImage {
            var metadata: Metadata? = null
            try {
                metadata = Metadata.fromJSON(File(settingsPath).readText())
            } catch (e: Exception) {

            }

            return LocalImage(filePath, settingsPath, metadata ?: Metadata())
        }
    }

    fun saveMetaData() {
        val json = metadata.toJSON()
        File(settingsPath).writeText(json)
    }

    val file: File by lazy {
        File(this.filePath)
    }

    override fun loadFullSize(context: Context): Promise<Bitmap> {
        return file.decodeBitmap()
    }

    override fun loadThumbnail(context: Context): Promise<Bitmap> {
        return loadFullSize(context).flatMap { fullBitmap ->
            fullBitmap.scale(200, 200, true)
        }
    }
}

data class RemoteImage(val remoteStorageLocation: String, override var metadata: Metadata) : Image {
    companion object {
        fun fromJSON(map: HashMap<String, Any>): RemoteImage? {
            val remoteStorageLocation = map["imageRef"] as? String ?: return null
            val metadataObj = map["settings"] as? HashMap<String, Any> ?: hashMapOf<String, Any>()
            val metadata = Metadata.fromJSON(metadataObj)
            return RemoteImage(remoteStorageLocation, metadata ?: Metadata())
        }
    }

    override fun loadFullSize(context: Context): Promise<Bitmap> {
        return ImageFetcher(context).fetchImage(FirebaseStorage.getInstance()
                .getReference(remoteStorageLocation))
    }

    override fun loadThumbnail(context: Context): Promise<Bitmap> {
        return loadFullSize(context).flatMap { fullBitmap ->
            fullBitmap.scale(200, 200, true)
        }
    }
}