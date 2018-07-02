package amal.global.amal

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wonderkiln.camerakit.*
import kotlinx.android.synthetic.main.fragment_capture.*


interface CaptureDelegate {
    fun settingsButtonTapped(fragment: CaptureFragment)
}

class CaptureFragment : Fragment() {

    private var lastLocation: Location? = null

    var delegate: CaptureDelegate? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_capture, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        shutterButton.setOnClickListener({ takePicture() })
        settingsButton.setOnClickListener({ delegate?.settingsButtonTapped(this) })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity!!.setTitle(R.string.title_capture)
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

        if (context!!.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, locationListener)
            lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        }
    }

    private fun takePicture() {
        cameraView.addCameraKitListener(object : CameraKitEventListener {
            override fun onEvent(event: CameraKitEvent) { }

            override fun onError(error: CameraKitError) { }

            override fun onImage(image: CameraKitImage) {

                val metadata = Metadata()
                metadata.latitude = lastLocation?.latitude ?: 0.0
                metadata.longitude = lastLocation?.longitude ?: 0.0

                PhotoStorage(activity!!).savePhotoLocally(image.jpeg, metadata)
            }

            override fun onVideo(video: CameraKitVideo) { }
        })
        cameraView.captureImage()
    }

    override fun onResume() {
        super.onResume()
        cameraView.start()
        beginListeningForLocation()
    }

    override fun onPause() {
        cameraView.stop()
        super.onPause()
    }

}
