package amal.global.amal

import org.json.JSONObject
import amal.global.amal.R.string.`object`
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import org.json.JSONArray
import org.json.JSONException
import java.security.InvalidParameterException

data class Metadata internal constructor(
        public var name: String = "",
        public var category: String = "",
        public var levelOfDamage: Int = 0,
        public var conditionNumber: Int = 0,
        public var hazards: Boolean = false,
        public var safetyHazards: Boolean = false,
        public var interventionRequired: Boolean = false,
        public var notes: String = "",
        public var latitude: Double = 0.0,
        public var longitude: Double = 0.0,
        public var localIdentifier: String = ""
) {

    fun coordinatesString(): String {
        if (hasCoordinates) {
            return "%.4f, %.4f".format(latitude, longitude)
        }
        return "No coordinates found"
    }

    val hasCoordinates: Boolean
        get() = !(latitude == 0.0 && longitude == 0.0)

    val coordinate: LatLng
        get() = LatLng(latitude, longitude)

    fun toJSONObject(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("name", name)
        jsonObject.put("category", category)
        jsonObject.put("levelOfDamage", levelOfDamage)
        jsonObject.put("conditionNumber", conditionNumber)
        jsonObject.put("hazards", hazards)
        jsonObject.put("safetyHazards", safetyHazards)
        jsonObject.put("interventionRequired", interventionRequired)
        jsonObject.put("notes", notes)
        jsonObject.put("latitude", latitude)
        jsonObject.put("longitude", longitude)
        jsonObject.put("localIdentifier", localIdentifier)
        return jsonObject
    }

    fun toMap(): Map<String, Any> {
        return toMap(toJSONObject())
    }

    fun toJSON(): String {
        return toJSONObject().toString(4)
    }

    companion object {

        //from a string on disk
        fun fromJSON(string: String): Metadata {
            val jsonObject = JSONObject(string)
            return this.fromJSON(toMap(jsonObject)) ?: Metadata()
        }

        //from a hashmap in firebase
        fun fromJSON(map: HashMap<String, Any>): Metadata? {
            try {
                val parser = Parser(map)
                return Metadata(
                        parser.fetch("name"),
                        parser.fetch("category"),
                        (parser.fetch<Long>("levelOfDamage")).toInt(),
                        (parser.fetch<Long>("conditionNumber")).toInt(),
                        parser.fetch("hazards"),
                        parser.fetch("safetyHazards"),
                        parser.fetch("interventionRequired"),
                        parser.fetch("notes"),
                        parser.fetch("latitude"),
                        parser.fetch("longitude"),
                        parser.fetch("localIdentifier")
                )
            } catch(e: Exception) {
                Log.d("amallog", e.message)
                return null
            }
        }
    }
}
