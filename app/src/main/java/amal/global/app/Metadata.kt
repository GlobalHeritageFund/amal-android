package global.amal.app

import com.google.android.gms.maps.model.LatLng
import com.squareup.moshi.JsonAdapter
import java.util.*
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.joda.time.DateTime
import org.joda.time.LocalDate

@JsonClass(generateAdapter = true)
data class Metadata internal constructor(
    var name: String = "",
    var category: String = "",
    var levelOfDamage: Int = 0,
    var conditionNumber: Int = 0,
    var hazards: Boolean = false,
    var safetyHazards: Boolean = false,
    var interventionRequired: Boolean = false,
    var notes: String = "",
    @Json(name="lat") var latitude: Double = 0.0,
    @Json(name="lon") var longitude: Double = 0.0,
    var date: Long? = null,
    var localIdentifier: String = "",
        //added 'hasBeenAssessed on Dec 8, 2o22 pics taken before then will not have this field
        //so code written to handle if field does not exist for back compatibility
    var hasBeenAssessed: Boolean = false
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

    val localDateValue: LocalDate?
        get() = date?.let { DateTime(date).toLocalDate() }

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
