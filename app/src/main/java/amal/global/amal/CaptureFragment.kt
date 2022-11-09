package amal.global.amal

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
    private var isSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            R.id.menu_item_flash -> {
                isSelected = !isSelected
                toggleFlash(isSelected)
                if (isSelected) {
                    item.icon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_flash_on_white_36pt)
                } else {
                    item.icon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_flash_off_white_36pt)
                }
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

    private fun takePicture() {
        cameraView.captureImage()
    }

    private fun toggleFlash(isSelected: Boolean) {
        Log.d("Capture Fragment", "toggleFlash method called")
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
            //TODO confirm waiting for fuse before saving the metadata isn't causing metadata for that image to be lost if doesn't return or something
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //TODO will switch this over to currentLocation instead of last location for more precision, but will make change when add loading spinner so delay not confusin
                fusedLocationClient.lastLocation
                        .addOnCompleteListener { task ->
                            val location = task.result
                            if (task.isSuccessful && location != null) {
                                metadata.latitude = location.latitude
                                metadata.longitude = location.longitude
                            } else {
                                Log.d("Capture fragment", "No location received: task success? ${task.isSuccessful}, location? ${location != null}")
                            }
                            //TODO may not need to launch this on a separate thread, but leaving it for now bc don't want to mess w things too much
                            GlobalScope.launch {
                                PhotoStorage(activity!!.applicationContext).savePhotoLocally(image.jpeg, metadata)
                            }
                        }
            } else {
                Log.d("Capture fragment", "Fine location permission not available")
            }
        }
        override fun onVideo(video: CameraKitVideo) { }
    }

}
