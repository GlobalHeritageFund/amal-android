package amal.global.amal

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.view.*
import com.wonderkiln.camerakit.*
import kotlinx.android.synthetic.main.fragment_capture.*
import android.animation.ObjectAnimator
import kotlinx.coroutines.experimental.launch


interface CaptureDelegate {
    fun settingsButtonTapped(fragment: CaptureFragment)
}

class CaptureFragment : Fragment() {

    private var lastLocation: Location? = null

    var delegate: CaptureDelegate? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_capture, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraView.addCameraKitListener(getCameraKitListener())
        shutterButton.setOnClickListener({ takePicture() })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_capture, menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity!!.setTitle(R.string.title_capture)
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
        requestLocationPermission()
        beginListeningForLocation()
    }

    override fun onPause() {
        cameraView.stop()
        super.onPause()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
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
        val permissionStatus = ActivityCompat.checkSelfPermission(activity!!, permission)
        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!, arrayOf(permission), 200)
        }
    }

    private fun beginListeningForLocation() {
        val locationManager = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                lastLocation = location
            }

            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

            override fun onProviderEnabled(provider: String) {}

            override fun onProviderDisabled(provider: String) {}
        }

        if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
            lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }
    }

    private fun takePicture() {
        cameraView.captureImage()
    }

    private fun animateFlashEmulator() {
        val anim = ObjectAnimator.ofFloat(flashEmulator, "alpha", 0f, 1f, 0f)
        anim.duration = 150
        anim.start()
    }

    private fun getCameraKitListener() = object : CameraKitEventListener {
        override fun onEvent(event: CameraKitEvent) { }

        override fun onError(error: CameraKitError) { }

        override fun onImage(image: CameraKitImage) {
            animateFlashEmulator()

            val metadata = Metadata()
            metadata.latitude = lastLocation?.latitude ?: 0.0
            metadata.longitude = lastLocation?.longitude ?: 0.0

            launch {
                PhotoStorage(activity!!.applicationContext).savePhotoLocally(image.jpeg, metadata)
            }
        }

        override fun onVideo(video: CameraKitVideo) { }
    }

}
