package amal.global.amal

import org.json.JSONObject


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
        public var firebaseImageKey: String = "",
        public var localIdentifier: String = ""
) {
    fun toJSON(): String {
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
        jsonObject.put("firebaseImageKey", firebaseImageKey)
        jsonObject.put("localIdentifier", localIdentifier)
        return jsonObject.toString(4)
    }

    companion object {
        fun fromJSON(string: String): Metadata {
            val jsonObject = JSONObject(string)
            return Metadata(
                    jsonObject.getString("name"),
                    jsonObject.getString("category"),
                    jsonObject.getInt("levelOfDamage"),
                    jsonObject.getInt("conditionNumber"),
                    jsonObject.getBoolean("hazards"),
                    jsonObject.getBoolean("safetyHazards"),
                    jsonObject.getBoolean("interventionRequired"),
                    jsonObject.getString("notes"),
                    jsonObject.getDouble("latitude"),
                    jsonObject.getDouble("longitude"),
                    jsonObject.getString("firebaseImageKey"),
                    jsonObject.getString("localIdentifier")
            )
        }
    }
}