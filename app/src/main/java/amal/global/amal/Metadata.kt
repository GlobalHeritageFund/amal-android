package amal.global.amal

import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.JsonAdapter
import java.util.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@JsonClass(generateAdapter = true)
data class Metadata internal constructor(
        public var name: String = "",
        public var category: String = "",
        public var levelOfDamage: Int = 0,
        public var conditionNumber: Int = 0,
        public var hazards: Boolean = false,
        public var safetyHazards: Boolean = false,
        public var interventionRequired: Boolean = false,
        public var notes: String = "",
        @Json(name="lat") public var latitude: Double = 0.0,
        @Json(name="lon") public var longitude: Double = 0.0,
        public var date: Long? = null,
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

    val dateValue: Date?
        get() = date?.let { Date(it) }

    val condition: String
        get() {
            return when (conditionNumber) {
                0 -> "unknown"
                1 -> "none"
                2 -> "minor"
                3 -> "moderate"
                4 -> "severe"
                5 -> "collapsed"
                else -> "unknown"
            }
        }

    val type: String
        get() {
            return when (category) {
                "area" -> "area"
                "site" -> "building"
                "object"-> "object"
                else -> "object"
            }

        }

    companion object {
        val jsonAdapter: JsonAdapter<Metadata>
            get() {
                val moshi = Moshi.Builder()
                        .add(KotlinJsonAdapterFactory())
                        .build()
                return moshi.adapter(Metadata::class.java)
            }

    }
}
