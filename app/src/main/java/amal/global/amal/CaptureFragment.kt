package amal.global.amal

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.wonderkiln.camerakit.*
import com.wonderkiln.camerakit.CameraKit.Constants.FLASH_OFF
import com.wonderkiln.camerakit.CameraKit.Constants.FLASH_ON
import kotlinx.android.synthetic.main.fragment_capture.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


interface CaptureDelegate {
    fun settingsButtonTapped(fragment: CaptureFragment)
}

class CaptureFragment : Fragment() {
    companion object {
        const val TAG = "Capture Fragment"
        const val CAMERA_PERMISSION_REQUEST = 1
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var orientationListener: OrientationEventListener
//    private lateinit var flashToRotate: View

    var delegate: CaptureDelegate? = null
    private var isSelected = false
    private var haveCameraPermission = true

//    val requestPermissionLauncher =
//            registerForActivityResult(ActivityResultContracts.RequestPermission()
//            ) { isGranted: Boolean ->
//                if (isGranted) {
//                    // All is good - do nothing
//                    Log.d(TAG, "permission granted")
//                } else {
//                    //change view to cover camera
//                    Log.d(TAG, "permission denied")
//                }
//            }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        orientationListener = object : OrientationEventListener(activity, SensorManager.SENSOR_DELAY_UI) {
            override fun onOrientationChanged(orientation: Int) {
                handleOrientationChange(orientation)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_capture, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_capture, menu)
//        flashToRotateItem = menu.findItem(R.id.menu_item_flash)
//        flashToRotate = flashToRotateItem.actionView
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().setTitle(R.string.title_capture)
    }


    override fun onResume() {
        super.onResume()
        //this permission was moved to here bc if user navigates away from amal to enable camera in settings
        //and then back to amal the view and permission was not getting updated
        //hopefully will not cause screen delay
        if (ContextCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED) {
            haveCameraPermission = false
        } else {
            haveCameraPermission = true
        }
        if (haveCameraPermission) {
            cameraView.addCameraKitListener(getNewCameraKitListener())
            shutterButton.setOnClickListener({ takePicture() })
            if (orientationListener.canDetectOrientation()) orientationListener.enable()
            cameraView.visibility = View.VISIBLE
            noCameraView.visibility = View.GONE
            cameraView.start()
            requestLocationPermission()
        } else {
            cameraView.visibility = View.GONE
            noCameraView.visibility = View.VISIBLE
        }
    }

    override fun onPause() {
        if (haveCameraPermission) {
            cameraView.stop()
            orientationListener?.disable()
        }
        super.onPause()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item!!.getItemId()) {
            R.id.menu_item_settings -> {
                delegate?.settingsButtonTapped(this)
                return true
            }
            R.id.menu_item_flash -> {
                if (haveCameraPermission) {
                    isSelected = !isSelected
                    toggleFlash(isSelected)
                    if (isSelected) {
                        item.icon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_flash_on_white_36pt)
                    } else {
                        item.icon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_flash_off_white_36pt)
                    }
                }
                return true
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
    }

    private fun handleOrientationChange(orientation: Int) {
        if (orientation >= 315 || orientation < 45) {
            shutterButton.animate().rotation(0F).start()
//            flashToRotate.animate().rotation(0F).start()
        } else if (orientation >=45 && orientation < 135) {
            shutterButton.animate().rotation(270F).start()
//            flashToRotate.animate().rotation(0F).start()
        } else if (orientation >=135 && orientation < 225) {
            shutterButton.animate().rotation(180F).start()
//            flashToRotate.animate().rotation(0F).start()
        } else if (orientation >=225 && orientation < 315) {
            shutterButton.animate().rotation(90F).start()
//            flashToRotate.animate().rotation(0F).start()
        } else {
            Log.d(TAG, "stall")
            //Keep the current State
        }
    }

    private fun requestLocationPermission() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        val permissionStatus = ActivityCompat.checkSelfPermission(requireActivity(), permission)
        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission), 200)
        }
    }

//    private fun requestCameraPermission() {
//        val permission = Manifest.permission.CAMERA
//        if (ContextCompat.checkSelfPermission(
//                        requireActivity(),
//                        Manifest.permission.CAMERA
//                ) == PackageManager.PERMISSION_GRANTED) {
//            return
//        } else {
//            requestPermissionLauncher.launch(
//                    Manifest.permission.CAMERA)
//        }
//    }

    private fun takePicture() {
        cameraView.captureImage()
    }

    //TODO not sure what would happen if camera did not have a flashlight can check for flashlight permissin before calling
    private fun toggleFlash(isSelected: Boolean) {
        //Could also do flash auto instead of flash on if desired
        if (isSelected) cameraView.flash = FLASH_ON
        else cameraView.flash = FLASH_OFF
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
