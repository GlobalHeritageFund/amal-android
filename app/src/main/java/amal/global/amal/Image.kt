package amal.global.amal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.storage.StorageManager
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.util.*
import kotlin.collections.HashMap

interface Image {
    fun load(context: Context) : GlideRequest<Drawable>
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

    val date: Date
        get() = metadata.date ?: Date(File(filePath).lastModified())

    override fun load(context: Context): GlideRequest<Drawable> {
        return GlideApp.with(context).load(file)
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

    val firebaseReference: StorageReference
        get() = FirebaseStorage.getInstance().getReference(remoteStorageLocation)

    override fun load(context: Context): GlideRequest<Drawable> {
        return GlideApp.with(context).load(firebaseReference)
    }
}