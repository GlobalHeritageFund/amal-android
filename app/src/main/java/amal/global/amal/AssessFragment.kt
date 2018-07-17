package amal.global.amal

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import kotlinx.android.synthetic.main.fragment_assess.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

interface AssessDelegate {
    fun mapTapped(fragment: AssessFragment)
    fun editLocationTapped(fragment: AssessFragment)
}

class AssessFragment : Fragment() {

    var image: LocalImage? = null

    var delegate: AssessDelegate? = null

    lateinit var mapView: MapView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_assess, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        explicitlyBindMapViewSoItDoesntGetDeallocatedForOnDestroy()

        nameField.afterTextChanged { editable: Editable? ->
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
            "area" -> categoryRadioGroup.check(R.id.radioOverall)
            "site" -> categoryRadioGroup.check(R.id.radioSite)
            "object" -> categoryRadioGroup.check(R.id.radioObject)
        }
        categoryRadioGroup.setOnCheckedChangeListener({ radioGroup, checkedID ->
            val string = when (checkedID) {
                R.id.radioOverall -> "area"
                R.id.radioSite -> "site"
                R.id.radioObject -> "object"
                else -> ""
            }
            image?.metadata?.category = string
            image?.saveMetaData()
        })

        val conditionButtons = listOf(
                conditionButton0,
                conditionButton1,
                conditionButton2,
                conditionButton3,
                conditionButton4,
                conditionButton5
        )

        conditionButtons.forEach({ it.isSelected = false })
        conditionButtons[image?.metadata?.conditionNumber ?: 0].isSelected = true

        conditionButtons.withIndex().forEach({ indexedValue ->
            indexedValue.value.setOnClickListener({ _ ->
                conditionButtons.forEach({ it.isSelected = false })
                indexedValue.value.isSelected = true
                image?.metadata?.conditionNumber = indexedValue.index
                image?.saveMetaData()
            })
        })

        hazardsCheckBox.isChecked = image?.metadata?.hazards ?: false
        hazardsCheckBox.setOnCheckedChangeListener({ buttonView, isChecked ->
            image?.metadata?.hazards = isChecked
            image?.saveMetaData()
        })

        safetyHazardsCheckBox.isChecked = image?.metadata?.safetyHazards ?: false
        safetyHazardsCheckBox.setOnCheckedChangeListener({ buttonView, isChecked ->
            image?.metadata?.safetyHazards = isChecked
            image?.saveMetaData()
        })

        interventionCheckBox.isChecked = image?.metadata?.interventionRequired ?: false
        interventionCheckBox.setOnCheckedChangeListener({ buttonView, isChecked ->
            image?.metadata?.interventionRequired = isChecked
            image?.saveMetaData()
        })


        notesField.setText(image?.metadata?.notes ?: "")
        notesField.afterTextChanged { editable: Editable? ->
            image?.metadata?.notes = editable.toString()
            image?.saveMetaData()
        }

        coordinatesTextView.text = image?.metadata?.coordinatesString()

        editLocationButton.text = if (hasCoordinates) "Edit Location" else "Set Location"

        editLocationButton.setOnClickListener({ view ->
            delegate?.editLocationTapped(this)
        })

        updateImage()
    }

    private fun updateImage() {
        image?.load(context!!)?.into(imageView)
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
