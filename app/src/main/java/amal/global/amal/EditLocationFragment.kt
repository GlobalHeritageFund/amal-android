package amal.global.amal

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_assess.*

class EditLocationFragment : Fragment() {

    var image: LocalImage? = null

    val coordinateOrNullIsland: LatLng
        get() = image?.metadata?.coordinate ?: LatLng(0.0, 0.0)

    val hasCoordinates: Boolean
        get() = image?.metadata?.hasCoordinates ?: false

    val zoomLevel: Float
        get() = if (hasCoordinates) 12.0f else 2.0f

    lateinit var mapView: MapView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        explicitlyBindMapViewSoItDoesntGetDeallocatedForOnDestroy()

        val bundle = savedInstanceState?.getBundle("MapViewBundleKey") ?: savedInstanceState
        mapView.onCreate(bundle)

        mapView.getMapAsync({ map ->

            val marker = MarkerOptions()
                    .position(coordinateOrNullIsland)
                    .title("Marker")
            map.addMarker(marker)

            val cameraPosition = CameraPosition
                    .builder()
                    .target(coordinateOrNullIsland)
                    .zoom(zoomLevel)
                    .build()
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

            map.setOnCameraMoveListener({
                editLocationButton.isEnabled = true
            })
        })

        editLocationButton.setOnClickListener({
            editLocationButton.isEnabled = false
            mapView.getMapAsync({
                val latLong = it.cameraPosition.target
                image?.metadata?.latitude = latLong.latitude
                image?.metadata?.longitude = latLong.longitude
                image?.saveMetaData()
            })
        })

    }

    fun explicitlyBindMapViewSoItDoesntGetDeallocatedForOnDestroy() {
        mapView = bind(R.id.mapView)
    }

    // MapView needs to have all of these forwarded manually
    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }
}
