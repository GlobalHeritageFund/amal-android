package global.amal.app

import global.amal.app.databinding.FragmentCaptureBinding
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.Preview
import androidx.camera.core.CameraSelector
import java.io.File
import androidx.camera.core.Camera
import android.view.ScaleGestureDetector
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks


interface CaptureDelegate {
    fun settingsButtonTapped(fragment: CaptureFragment)
}

class CaptureFragment : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocationCancellationTokenSource: CancellationTokenSource? = null
    private var ongoingLocationTask: Task<Location?>? = null

    companion object {
        private const val TAG = "Capture Fragment"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        //for now not worrying about read_external_storage unless that is a feature that is in demand
        //will have
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_MEDIA_LOCATION
//                Manifest.permission.READ_EXTERNAL_STORAGE
                //location permissions are not necessarily required, but deciding to force it for now
                //currently not requesting READ_EXTERNAL_STORAGE (or READ_MEDIA_IMAGES for api 33+) bc relying on photopicker
            ).toTypedArray()
        private const val MAX_LOCATION_AGE_MS: Long = 1 * 60 * 1000 //sets oldest lastLocation used to one minute
        private const val MAX_CURRENT_LOCATION_WAIT: Long = 10 * 1000 //sets timeout for currentLocation to 10 seconds
        private const val FLASH_MODE_STATE_AUTO = 1
        private const val FLASH_MODE_STATE_ON = 2
        private const val FLASH_MODE_STATE_OFF = 3
    }

    private lateinit var viewBinding: FragmentCaptureBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    var delegate: CaptureDelegate? = null
    private var camera: Camera? = null // To hold the Camera instance
    private lateinit var scaleGestureDetector: ScaleGestureDetector // For pinch gesture detection
    private var flashNumber = FLASH_MODE_STATE_AUTO
    private var flashMenuItem: MenuItem? = null


    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    requireContext(),
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
                viewBinding.noCameraView.visibility = View.VISIBLE
                viewBinding.viewFinder.visibility = View.GONE
            } else {
                //think the first 2 lines are unnecessary, but to avoid making too many changes will leave
                viewBinding.noCameraView.visibility = View.GONE
                viewBinding.viewFinder.visibility = View.VISIBLE
                startCamera()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)
        viewBinding = FragmentCaptureBinding.inflate(layoutInflater)
        val view = viewBinding.root
        viewBinding.shutterButton.setOnClickListener { takePhoto() }
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_capture, menu)
        // Get a reference to the flash menu item
        flashMenuItem = menu.findItem(R.id.menu_item_flash)
        // Set the initial icon based on the current flashNumber state
        updateFlashMenuItemIcon()
    }

    //permissions and adding listeners moved here
    //in camerax was in onCreate
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = getString(R.string.title_capture)
        // Request required camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onPause() {
        super.onPause()
        // Cancel any ongoing location request when the fragment is paused
        currentLocationCancellationTokenSource?.cancel()
        Log.d(TAG, "onPause: Cancelled ongoing location request if any.")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        flashMenuItem = null
        currentLocationCancellationTokenSource?.cancel()
        currentLocationCancellationTokenSource = null
        Log.d(TAG, "onDestroyView: Cleaned up resources.")
    }

    override fun onDetach() {
        delegate = null
        super.onDetach()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_settings -> {
                delegate?.settingsButtonTapped(this)
                true
            }

            R.id.menu_item_flash -> {
                toggleFlashMode()
                true
            }

            else ->
                super.onOptionsItemSelected(item)
        }
    }

    private fun toggleFlashMode() {
        flashNumber = (flashNumber % 3) + 1
        imageCapture?.flashMode = getCurrentImageCaptureFlashMode()
        updateFlashMenuItemIcon()

        // Log and show a Toast (could remove)
        val modeText = when (flashNumber) {
            FLASH_MODE_STATE_AUTO -> "Auto"
            FLASH_MODE_STATE_ON -> "On"
            FLASH_MODE_STATE_OFF -> "Off"
            else -> "Unknown"
        }
        Log.d(TAG, "Flash mode set to: $modeText")
        Toast.makeText(requireContext(), "Flash: $modeText", Toast.LENGTH_SHORT).show()
    }

    private fun getCurrentImageCaptureFlashMode(): Int {
        return when (flashNumber) {
            FLASH_MODE_STATE_AUTO -> ImageCapture.FLASH_MODE_AUTO
            FLASH_MODE_STATE_ON -> ImageCapture.FLASH_MODE_ON
            FLASH_MODE_STATE_OFF -> ImageCapture.FLASH_MODE_OFF
            else -> ImageCapture.FLASH_MODE_AUTO // Default fallback
        }
    }

    private fun updateFlashMenuItemIcon() {
        flashMenuItem?.let { menuItem -> // Use the stored reference
            when (flashNumber) {
                FLASH_MODE_STATE_AUTO -> menuItem.setIcon(R.drawable.ic_flash_auto_white_36pt)
                FLASH_MODE_STATE_ON -> menuItem.setIcon(R.drawable.ic_flash_on_white_36pt)
                FLASH_MODE_STATE_OFF -> menuItem.setIcon(R.drawable.ic_flash_off_white_36pt)
                else -> {}
            }
        }
    }


    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = this.imageCapture ?: run {
            Log.w(TAG, "ImageCapture not initialized yet.")
            return
        }

        //naming the image based on data for simplicity
        //defaulting to us way of formatting date (e.g month, day, year)
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
        .format(System.currentTimeMillis())

        val imageDir = File(requireContext().filesDir, "images")
        if (!imageDir.exists() && !imageDir.mkdirs()) {
            Log.e(TAG, "Failed to create image directory: $imageDir")
            Toast.makeText(requireContext(), "Error: Could not create image directory", Toast.LENGTH_LONG).show()
            return
        }
        val photoFile = File(imageDir, "$name.jpeg")
        val metaFile = File(imageDir, "$name.json")
        // Prepare the output file options for the ImageCapture use case
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken,, right now using requireContext but might change to requireActivity
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(requireContext(), "Photo capture failed: ${exc.localizedMessage}", Toast.LENGTH_LONG).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    getLocationForMetadata(metaFile)
                    if (!isAdded) return
                    Log.d(TAG, "Photo capture succeeded: ${output.savedUri}")
                    Toast.makeText(context, "Photo saved to ${output.savedUri}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }
            imageCapture = ImageCapture.Builder().setFlashMode(getCurrentImageCaptureFlashMode()).build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA


            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera AND STORE THE CAMERA INSTANCE
                this.camera = cameraProvider.bindToLifecycle( // Assign to the member variable
                    this, cameraSelector, preview, imageCapture
                )
                // NOW that 'this.camera' is initialized, setup pinch to zoom
                setupPinchToZoom()
                observeZoomState() // Optional: for logging zoom changes
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    //This part added to change from lastLocation to currentLocation
    //A bit worried by this because of possible lag so more chance of no location bc fragment isn't attached or a timeout occurred
    //Also if user takes next picture before previous picture location is obtained then location for first pic will be lost
    //Finally, the user won't know for sure if current location or last location was received
    //For now I am not implementing a minimum location difference, but may add in later
    @SuppressLint("MissingPermission")
    private fun getLocationForMetadata(settingsFile: File) {
        if (!isAdded) {
            Log.w(TAG, "Fragment not added, aborting getLocationForMetadata.")
            return
        }
        if (!allPermissionsGranted()) {
            Log.w(TAG, "Location permission not granted. Saving metadata without location.")
            saveMetadataInternal(null, settingsFile)
            return
        }

        // Step 1: Try lastLocation

        // Check if there is an ongoing task that is not yet complete
        val taskToUse: Task<Location?>

        if (ongoingLocationTask != null && ongoingLocationTask?.isComplete==false) {
            Log.d(TAG, "An existing location task is already running. Reusing it.")
            taskToUse = ongoingLocationTask!!
        } else {
            Log.d(TAG, "No active location task. Starting a new one.")
            // Create and assign the new task
            taskToUse = fusedLocationClient.lastLocation
                .continueWithTask { task ->
                    if (!isAdded) { // Check fragment state before proceeding
                        Log.w(TAG, "Fragment detached during lastLocation check, cancelling.")
                        return@continueWithTask Tasks.forCanceled<Location>()
                    }

                    val lastLocation = task.result

                    if (task.isSuccessful && lastLocation != null) {
                        val locationAge = System.currentTimeMillis() - lastLocation.time
                        // Optional: val isAccurateEnough = lastLocation.accuracy < MIN_LAST_LOCATION_ACCURACY_METERS

                        if (locationAge < MAX_LOCATION_AGE_MS /* && isAccurateEnough */) {
                            Log.i(TAG, "Using recent last known location.")
                            // Directly return a successful Task with this location
                            return@continueWithTask Tasks.forResult(lastLocation)
                        }
                    }
                    Log.d(TAG, "Last location not suitable. Attempting current location.")
                    return@continueWithTask fetchCurrentLocationTask()
                }
            // Store the new task so subsequent calls can reuse it
            ongoingLocationTask = taskToUse
        }
        taskToUse.addOnCompleteListener { task ->
            // When the task completes (either success or fail), clear the ongoing task variable
            // so the NEXT capture after this burst can start a fresh request.
            if (taskToUse == ongoingLocationTask) {
                ongoingLocationTask = null
            }

            if (!isAdded) {
                Log.w(TAG, "Fragment detached when location task completed for ${settingsFile.name}.")
                return@addOnCompleteListener
            }
            if (task.isSuccessful) {
                val finalLocation = task.result // This will be either suitable lastLocation or result of getCurrentLocation
                Log.d(TAG, "Final location determined for ${settingsFile.name}: ${if (finalLocation != null) "Lat ${finalLocation.latitude}" else "null"}  Saving metadata")
                saveMetadataInternal(finalLocation, settingsFile)
            } else {
                Log.e(TAG, "Failed to get any suitable location for ${settingsFile.name}.  Saving metadata.", task.exception)
                saveMetadataInternal(null, settingsFile) // Save without location on ultimate failure
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocationTask(): Task<Location?> { // Returns Task<Location?> to allow null
        if (!isAdded) return Tasks.forCanceled() // Or Tasks.forResult(null) if preferred

        currentLocationCancellationTokenSource?.cancel()
        currentLocationCancellationTokenSource = CancellationTokenSource()
        val cancellationToken = currentLocationCancellationTokenSource!!.token

        val currentLocationRequest = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setDurationMillis(MAX_CURRENT_LOCATION_WAIT) // Timeout for the request
            .setMaxUpdateAgeMillis(MAX_LOCATION_AGE_MS) // Optional: allow slightly older cached current location
            .build()

        Log.d(TAG, "Fetching current location (Task)...")
        // FusedLocationProviderClient.getCurrentLocation can return a Task<Location> which might be null
        // To make it explicitly Task<Location?>, we can wrap it or handle null in a subsequent step.
        // For simplicity, we'll let the type be Task<Location> and handle potential nullity in the final onCompleteListener.
        // Or, more robustly, map it:
        return fusedLocationClient.getCurrentLocation(currentLocationRequest, cancellationToken)
            .continueWith { task -> // This 'continueWith' is just to explicitly handle the Task<Location> -> Location?
                if (task.isSuccessful) {
                    task.result // This can be null if getCurrentLocation couldn't get a fix within the duration
                } else {
                    // If getCurrentLocation itself fails (e.g., timeout, provider disabled)
                    Log.w(TAG, "getCurrentLocation task failed.", task.exception)
                    throw task.exception ?: IllegalStateException("Unknown error in getCurrentLocation task")
                }
            }
    }

    private fun saveMetadataInternal(location: Location?, settingsFile: File) {
        val metadata = Metadata()
        metadata.date = System.currentTimeMillis()

        if (location != null) {
            metadata.latitude = location.latitude
            metadata.longitude = location.longitude
            Log.d(TAG, "Saving metadata with location: Lat ${location.latitude}, Lon ${location.longitude}")
        } else {
            // Handle case where location is null (e.g., set to 0.0, NaN, or leave as default in Metadata class)
            metadata.latitude = 0.0 // Or some indicator of no location
            metadata.longitude = 0.0
            Log.w(TAG, "Saving metadata without location information.")
        }

        try {
            settingsFile.writeText(Metadata.jsonAdapter.toJson(metadata))
            Log.d(TAG, "Metadata saved to ${settingsFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Error writing metadata file: ${e.message}", e)
            if(isAdded) Toast.makeText(context, "Error saving metadata", Toast.LENGTH_SHORT).show()
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupPinchToZoom() {
        val currentCamera = this.camera ?: return // Exit if camera is not yet available

        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val cameraInfo = currentCamera.cameraInfo
                val currentZoomRatio = cameraInfo.zoomState.value?.zoomRatio ?: 1.0f
                val delta = detector.scaleFactor // How much to zoom

                val newZoomRatio = (currentZoomRatio * delta).coerceIn(
                    cameraInfo.zoomState.value?.minZoomRatio ?: 1.0f,
                    cameraInfo.zoomState.value?.maxZoomRatio ?: 1.0f
                )
                Log.d(TAG, "Zooming: current=$currentZoomRatio, delta=$delta, new=$newZoomRatio")
                currentCamera.cameraControl.setZoomRatio(newZoomRatio)
                return true // Gesture handled
            }
        }

        scaleGestureDetector = ScaleGestureDetector(requireContext(), listener)
        // Attach the gesture detector to your PreviewView (viewFinder)
        viewBinding.viewFinder.setOnTouchListener { _, event ->
            // Let the ScaleGestureDetector inspect all events.
            scaleGestureDetector.onTouchEvent(event)
            // Return true to indicate the event was consumed by the PreviewView,
            // preventing it from propagating further if the touch is on the PreviewView.
            // You could also return 'handled' if you only want to consume the event
            // when a scale gesture actually occurs.
            return@setOnTouchListener true
        }

    }
    // Optional: To log zoom state changes or update a UI element like a zoom slider
    private fun observeZoomState() {
        val currentCamera = this.camera ?: return
        // Observe on viewLifecycleOwner to prevent observing when view is destroyed
        currentCamera.cameraInfo.zoomState.observe(viewLifecycleOwner) { zoomState ->
            Log.d(TAG, "Current Zoom Ratio: ${zoomState.zoomRatio}")
            Log.d(TAG, "Min Zoom Ratio: ${zoomState.minZoomRatio}")
            Log.d(TAG, "Max Zoom Ratio: ${zoomState.maxZoomRatio}")
            // update UI elements here if needed
        }
    }
}




