package amal.global.amal

import org.json.JSONObject
import java.io.File

data class Image internal constructor(
        public var filePath: String,
        public var settingsPath: String,
        public var metadata: Metadata
) {
    companion object {
        fun convenience(filePath: String, settingsPath: String) : Image {
            var metadata: Metadata? = null
            try {
                metadata = Metadata.fromJSON(File(settingsPath).readText())
            } catch (e: Exception) {

            }

            return Image(filePath, settingsPath, metadata ?: Metadata())
        }
    }

    fun saveMetaData() {
        val json = metadata.toJSON()
        File(settingsPath).writeText(json)
    }

    val file: File by lazy {
        File(this.filePath)
    }

}
