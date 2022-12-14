package amal.global.amal

import amal.global.amal.databinding.FragmentEditLocationBinding
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

interface EditLocationFragmentDelegate {
    fun returnToAssessFragment(fragment: EditLocationFragment)
}
class EditLocationFragment : Fragment() {

    private var _binding: FragmentEditLocationBinding? = null
    private val binding get() = _binding!!

    //setting pre-existing coordinate as coordinates of first image
    var imageList: List<LocalImage>? = null
    var image: LocalImage? = imageList?.get(0)

    val coordinateOrNullIsland: LatLng
        get() = image?.metadata?.coordinate ?: LatLng(0.0, 0.0)

    val hasCoordinates: Boolean
        get() = image?.metadata?.hasCoordinates ?: false

    val zoomLevel: Float
        get() = if (hasCoordinates) 12.0f else 2.0f

    var delegate: EditLocationFragmentDelegate? = null

    lateinit var mapView: MapView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentEditLocationBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        explicitlyBindMapViewSoItDoesntGetDeallocatedForOnDestroy()

        val bundle = savedInstanceState?.getBundle("MapViewBundleKey") ?: savedInstanceState
        mapView.onCreate(bundle)

        mapView.getMapAsync { map ->

            if (hasCoordinates) {
                val marker = MarkerOptions()
                        .position(coordinateOrNullIsland)
                        .title("Marker")
                map.addMarker(marker)
            }

            map.mapType = GoogleMap.MAP_TYPE_SATELLITE

            val cameraPosition = CameraPosition
                    .builder()
                    .target(coordinateOrNullIsland)
                    .zoom(zoomLevel)
                    .build()
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

            map.setOnCameraMoveListener {
                binding.editLocationButton.isEnabled = true
            }
        }

        binding.editLocationButton.setOnClickListener {
            binding.editLocationButton.isEnabled = false
            mapView.getMapAsync {
                val latLong = it.cameraPosition.target
                imageList?.forEach {
                    it.metadata?.latitude = latLong.latitude
                    it.metadata?.longitude = latLong.longitude
                    it.saveMetaData()
                }
                delegate?.returnToAssessFragment(this)
            }
        }

    }

    fun explicitlyBindMapViewSoItDoesntGetDeallocatedForOnDestroy() {
        mapView = bind(R.id.mapView)
    }

    // MapView needs to have all of these forwarded manually
    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
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
