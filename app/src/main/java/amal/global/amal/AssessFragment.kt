package amal.global.amal

import amal.global.amal.databinding.FragmentAssessBinding
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.Editable
import android.view.*
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar

interface AssessDelegate {
    fun mapTapped(fragment: AssessFragment)
    fun editLocationTapped(fragment: AssessFragment)
    fun deleteButtonTapped(fragment: AssessFragment, imageList: List<LocalImage>?)
    fun saveButtonTapped(fragment: AssessFragment)
}

class AssessFragment : Fragment() {

    private var _binding: FragmentAssessBinding? = null
    private val binding get() = _binding!!

    var imageList: List<LocalImage> = emptyList()

    var delegate: AssessDelegate? = null

    lateinit var mapView: MapView

    val conditionButtons: List<Button>
        get() {
            return listOf(
                    binding.conditionButton0,
                    binding.conditionButton1,
                    binding.conditionButton2,
                    binding.conditionButton3,
                    binding.conditionButton4,
                    binding.conditionButton5
            )
        }

    val conditionLabels = listOf(
            "Condition unknown.",
            "No damage, good condition.",
            "Minor damage, fair condition.",
            "Moderate damage, poor condition.",
            "Severe damage, very bad condition.",
            "Collapsed, destroyed."
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        _binding = FragmentAssessBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        just setting fields based on first item on list
//        if only one image selected is correct
//        if more than one selected have to choose one randomly it conflicting data so might as well be the first one
        super.onViewCreated(view, savedInstanceState)

        explicitlyBindMapViewSoItDoesntGetDeallocatedForOnDestroy()
        val image: LocalImage? = imageList.getOrNull(0)

        binding.nameField.setText(image?.metadata?.name ?: "")
        binding.nameField.afterTextChanged { editable: Editable? ->
            image?.metadata?.name = editable.toString()
            image?.saveMetaData()
        }

        val hasCoordinates = image?.metadata?.hasCoordinates ?: false

        if (!hasCoordinates) {
            val layout = mapView.layoutParams
            layout.height = 0
            mapView.requestLayout()
        }



        val bundle = savedInstanceState?.getBundle("MapViewBundleKey") ?: savedInstanceState
        mapView.onCreate(bundle)

        mapView.getMapAsync { map ->
            map.setOnMapClickListener {
                delegate?.mapTapped(this@AssessFragment)
            }

            //can take this out if want to revert to old map type for this view and satellite only for edit view
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE

            map.uiSettings.setAllGesturesEnabled(false)
            image?.metadata?.let {
                if (!it.hasCoordinates) {
                    return@getMapAsync
                }
                val marker = MarkerOptions()
                        .position(it.coordinate)
                        .title("Marker")
                map.addMarker(marker)
                val cameraPosition = CameraPosition.builder().target(it.coordinate).zoom(12.0f).build()
                map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
        }

        when (image?.metadata?.category ?: "") {
            "area" -> binding.categoryRadioGroup.check(R.id.radioOverall)
            "site" -> binding.categoryRadioGroup.check(R.id.radioSite)
            "object" -> binding.categoryRadioGroup.check(R.id.radioObject)
        }
        binding.categoryRadioGroup.setOnCheckedChangeListener { radioGroup, checkedID ->
            val string = when (checkedID) {
                R.id.radioOverall -> "area"
                R.id.radioSite -> "site"
                R.id.radioObject -> "object"
                else -> ""
            }
            imageList.forEach {
                it.metadata?.category = string
                it.saveMetaData()
            }

        }

        activateConditionButtonAtIndex(image?.metadata?.conditionNumber ?: 0)

        conditionButtons.withIndex().forEach { indexedValue ->
            indexedValue.value.setOnClickListener { _ ->
                activateConditionButtonAtIndex(indexedValue.index)
                imageList.forEach {
                    it.metadata?.conditionNumber = indexedValue.index
                    it.saveMetaData()
                }

            }
        }

        binding.hazardsCheckBox.isChecked = image?.metadata?.hazards ?: false
        binding.hazardsCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            imageList.forEach {
                it.metadata?.hazards = isChecked
                it.saveMetaData()
            }
        }

        binding.safetyHazardsCheckBox.isChecked = image?.metadata?.safetyHazards ?: false
        binding.safetyHazardsCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            imageList.forEach {
                it.metadata?.safetyHazards = isChecked
                it.saveMetaData()
            }
        }

        binding.interventionCheckBox.isChecked = image?.metadata?.interventionRequired ?: false
        binding.interventionCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            imageList.forEach {
                it.metadata?.interventionRequired = isChecked
                it.saveMetaData()
            }
        }


        binding.notesField.setText(image?.metadata?.notes ?: "")
        binding.notesField.afterTextChanged { editable: Editable? ->
            imageList.forEach{
                it.metadata?.notes = editable.toString()
                it.saveMetaData()
            }
        }

        binding.coordinatesTextView.text = image?.metadata?.coordinatesString()
        binding.editLocationButton.text = if (hasCoordinates) "Edit Location" else "Set Location"

        binding.editLocationButton.setOnClickListener { view ->
            if (haveNetwork(requireContext())) {
                delegate?.editLocationTapped(this)
            } else {
                Snackbar.make(binding.root, "Cannot edit location without a network connection.", Snackbar.LENGTH_LONG).show()
            }
        }

        updateImageView()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_assess, menu)
    }

    fun activateConditionButtonAtIndex(index: Int) {
        conditionButtons.forEach({ it.isActivated = false })
        conditionButtons[index].isActivated = true
        binding.conditionDescription.text = conditionLabels[index]
    }

    private fun updateImageView() {
        if (imageList.isNotEmpty()) {
            if (imageList.size > 1) {
                binding.imageView.visibility = View.GONE
                binding.multiAssessText.text = getString(R.string.batch_assess_header,imageList.size.toString())
                binding.multiAssessText.visibility = View.VISIBLE
            } else {
                imageList.getOrNull(0)?.load(requireContext())?.into(binding.imageView)
            }
        }
    }

    fun explicitlyBindMapViewSoItDoesntGetDeallocatedForOnDestroy() {
        mapView = bind(R.id.mapView)
    }

    private fun haveNetwork(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return connectivityManager.activeNetwork != null
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo ?: return false
            return networkInfo.isConnectedOrConnecting
        }
    }

    private fun createDeleteAlert() {
        val builder = AlertDialog.Builder(requireContext());
        builder.setMessage("This will permanently delete your selected images. Do you want to continue?");
        builder.setPositiveButton(R.string.delete) { dialog, which ->
            delegate?.deleteButtonTapped(this, imageList)
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") {dialog, which -> dialog.dismiss()}
        builder.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.menu_item_delete -> {
                //I think this is non-intuitive and should not be here
                //I would not think the delete button would delete the picture, only the draft assessment
                //with delete functionality on gallery page active, this seems unnecessary and potentially confusing
                createDeleteAlert()
                return true
            }
            R.id.menu_item_save -> {
                delegate?.saveButtonTapped(this)
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }
}
