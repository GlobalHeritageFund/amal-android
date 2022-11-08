package amal.global.amal

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.wonderkiln.camerakit.*
import kotlinx.android.synthetic.main.fragment_capture.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


interface CaptureDelegate {
    fun settingsButtonTapped(fragment: CaptureFragment)
}

class CaptureFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    var delegate: CaptureDelegate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //TODO make sure sending correct context to location client - may need to be main activity
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_capture, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraView.addCameraKitListener(getNewCameraKitListener())
        shutterButton.setOnClickListener({ takePicture() })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_capture, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().setTitle(R.string.title_capture)
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
        requestLocationPermission()
//        beginListeningForLocation()
    }

    override fun onPause() {
        cameraView.stop()
        super.onPause()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item!!.getItemId()) {
            R.id.menu_item_settings -> {
                delegate?.settingsButtonTapped(this)
                return true
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
    }

    private fun requestLocationPermission() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        val permissionStatus = ActivityCompat.checkSelfPermission(requireActivity(), permission)
        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), 200)
        }
    }

//    @SuppressLint("MissingPermission")
//    private fun beginListeningForLocation() {
//        Log.d("cap frag last loc","got in beginListening")
//        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
//
//        val locationListener = object : LocationListener {
//            override fun onLocationChanged(location: Location) {
//                lastLocation = location
//            }
//
//            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
//
//            override fun onProviderEnabled(provider: String) {}
//
//            override fun onProviderDisabled(provider: String) {}
//        }
//
//        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            Log.d("cap frag last loc","got in if loc permissions")
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
//            lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
//        } else {
//            Log.d("cap frag","hit else in permission location")
//        }
//
//    }

    private fun takePicture() {
        cameraView.captureImage()
    }

    private fun animateFlashEmulator() {
        val anim = ObjectAnimator.ofFloat(flashEmulator, "alpha", 0f, 1f, 0f)
        anim.duration = 150
        anim.start()
    }

    private fun getNewCameraKitListener() = object : CameraKitEventListener {
        override fun onEvent(event: CameraKitEvent) { }

        override fun onError(error: CameraKitError) { }

        override fun onImage(image: CameraKitImage) {
            animateFlashEmulator()

            val metadata = Metadata()
            metadata.date = System.currentTimeMillis()

            //TODO check GPS or network? also see if okay with only checking once
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d("Cap frag", "if in on image")
                fusedLocationClient.lastLocation
                        .addOnSuccessListener { location->
                            if (location != null) {
                                Log.d("Cap Frag Longitude is ", location.longitude.toString() )
                                Log.d("Cap Frag Latitude is ", location.latitude.toString() )
                                metadata.latitude = location.latitude
                                metadata.longitude = location.longitude
                            } else {
                                Log.d("Cap frag", "lastlocation Success but No location received")
                            }
                        }.addOnFailureListener{ error->
                            Log.d("capfrag lastloc fail",error.toString())
                        }
            } else {
                Log.d("Cap frag", "else in on image")
            }

            GlobalScope.launch {
                PhotoStorage(activity!!.applicationContext).savePhotoLocally(image.jpeg, metadata)
            }
        }

        override fun onVideo(video: CameraKitVideo) { }
    }

}
