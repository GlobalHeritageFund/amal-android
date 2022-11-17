package amal.global.amal

import amal.global.amal.databinding.FragmentAssessBinding
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.Editable
import android.util.Log
import android.view.*
import android.widget.Button
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

interface AssessDelegate {
    fun mapTapped(fragment: AssessFragment)
    fun editLocationTapped(fragment: AssessFragment)
    fun deleteButtonTapped(fragment: AssessFragment, imagePath: String?, settingsPath: String?)
}

class AssessFragment : Fragment() {

    private var _binding: FragmentAssessBinding? = null
    private val binding get() = _binding!!

    var image: LocalImage? = null

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
        super.onViewCreated(view, savedInstanceState)

        explicitlyBindMapViewSoItDoesntGetDeallocatedForOnDestroy()

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

        mapView.getMapAsync({ map ->
            map.setOnMapClickListener {
                delegate?.mapTapped(this@AssessFragment)
            }

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
        })

        when (image?.metadata?.category ?: "") {
            "area" -> binding.categoryRadioGroup.check(R.id.radioOverall)
            "site" -> binding.categoryRadioGroup.check(R.id.radioSite)
            "object" -> binding.categoryRadioGroup.check(R.id.radioObject)
        }
        binding.categoryRadioGroup.setOnCheckedChangeListener({ radioGroup, checkedID ->
            val string = when (checkedID) {
                R.id.radioOverall -> "area"
                R.id.radioSite -> "site"
                R.id.radioObject -> "object"
                else -> ""
            }
            image?.metadata?.category = string
            image?.saveMetaData()
        })

        activateConditionButtonAtIndex(image?.metadata?.conditionNumber ?: 0)

        conditionButtons.withIndex().forEach({ indexedValue ->
            indexedValue.value.setOnClickListener({ _ ->
                activateConditionButtonAtIndex(indexedValue.index)
                image?.metadata?.conditionNumber = indexedValue.index
                image?.saveMetaData()
            })
        })

        binding.hazardsCheckBox.isChecked = image?.metadata?.hazards ?: false
        binding.hazardsCheckBox.setOnCheckedChangeListener({ buttonView, isChecked ->
            image?.metadata?.hazards = isChecked
            image?.saveMetaData()
        })

        binding.safetyHazardsCheckBox.isChecked = image?.metadata?.safetyHazards ?: false
        binding.safetyHazardsCheckBox.setOnCheckedChangeListener({ buttonView, isChecked ->
            image?.metadata?.safetyHazards = isChecked
            image?.saveMetaData()
        })

        binding.interventionCheckBox.isChecked = image?.metadata?.interventionRequired ?: false
        binding.interventionCheckBox.setOnCheckedChangeListener({ buttonView, isChecked ->
            image?.metadata?.interventionRequired = isChecked
            image?.saveMetaData()
        })


        binding.notesField.setText(image?.metadata?.notes ?: "")
        binding.notesField.afterTextChanged { editable: Editable? ->
            image?.metadata?.notes = editable.toString()
            image?.saveMetaData()
        }

        binding.coordinatesTextView.text = image?.metadata?.coordinatesString()
        binding.editLocationButton.text = if (hasCoordinates) "Edit Location" else "Set Location"

        binding.editLocationButton.setOnClickListener({ view ->
            delegate?.editLocationTapped(this)
        })

        updateImage()
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

    private fun updateImage() {
        image?.load(requireContext())?.into(binding.imageView)
    }

    fun explicitlyBindMapViewSoItDoesntGetDeallocatedForOnDestroy() {
        mapView = bind(R.id.mapView)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.menu_item_delete -> {
                Log.d("assess fragment", "delete item option selected")
                delegate?.deleteButtonTapped(this, image?.filePath, image?.settingsPath)
                return true
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
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
        super.onDestroyView()
        _binding = null
    }

    override fun onStop() {
        mapView.onStop()
        super.onStop()
    }
}
