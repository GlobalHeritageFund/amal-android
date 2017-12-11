package amal.global.amal

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

class AssessFragment : Fragment() {

    var image: LocalImage? = null

    lateinit var imageView: ImageView
    lateinit var nameField: EditText
    lateinit var categoryRadioGroup: RadioGroup
    lateinit var conditionRadioGroup: RadioGroup
    lateinit var hazardsCheckBox: CheckBox
    lateinit var safetyHazardsCheckBox: CheckBox
    lateinit var interventionCheckBox: CheckBox
    lateinit var notesField: EditText
    lateinit var coordinatesTextView: TextView

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_assess, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageView = bind(R.id.image_view)

        nameField = bind(R.id.name_field)
        nameField.setText(image?.metadata?.name ?: "")
        nameField.afterTextChanged { editable: Editable? ->
            image?.metadata?.name = editable.toString()
            image?.saveMetaData()
        }

        categoryRadioGroup = bind(R.id.category_radio_group)
        when (image?.metadata?.category ?: "") {
            "area" -> categoryRadioGroup.check(R.id.radio_overall)
            "site" -> categoryRadioGroup.check(R.id.radio_site)
            "object" -> categoryRadioGroup.check(R.id.radio_object)
        }
        categoryRadioGroup.setOnCheckedChangeListener({ radioGroup, checkedID ->
            val string = when (checkedID) {
                R.id.radio_overall -> "area"
                R.id.radio_site -> "site"
                R.id.radio_object -> "object"
                else -> ""
            }
            image?.metadata?.category = string
            image?.saveMetaData()
        })

        conditionRadioGroup = bind(R.id.condition_radio_group)
        when (image?.metadata?.conditionNumber ?: 0) {
            1 -> conditionRadioGroup.check(R.id.radio_condition_1)
            2 -> conditionRadioGroup.check(R.id.radio_condition_2)
            3 -> conditionRadioGroup.check(R.id.radio_condition_3)
            4 -> conditionRadioGroup.check(R.id.radio_condition_4)
            5 -> conditionRadioGroup.check(R.id.radio_condition_5)
        }
        conditionRadioGroup.setOnCheckedChangeListener({ radioGroup, checkedID ->
            val value = when (checkedID) {
                R.id.radio_condition_1 -> 1
                R.id.radio_condition_2 -> 2
                R.id.radio_condition_3 -> 3
                R.id.radio_condition_4 -> 4
                R.id.radio_condition_5 -> 5
                else -> 0
            }
            image?.metadata?.conditionNumber = value
            image?.saveMetaData()
        })

        hazardsCheckBox = bind(R.id.checkbox_hazards)
        hazardsCheckBox.isChecked = image?.metadata?.hazards ?: false
        hazardsCheckBox.setOnCheckedChangeListener({ buttonView, isChecked ->
            image?.metadata?.hazards = isChecked
            image?.saveMetaData()
        })

        safetyHazardsCheckBox = bind(R.id.checkbox_safety)
        safetyHazardsCheckBox.isChecked = image?.metadata?.safetyHazards ?: false
        safetyHazardsCheckBox.setOnCheckedChangeListener({ buttonView, isChecked ->
            image?.metadata?.safetyHazards = isChecked
            image?.saveMetaData()
        })

        interventionCheckBox = bind(R.id.checkbox_intervention)
        interventionCheckBox.isChecked = image?.metadata?.interventionRequired ?: false
        interventionCheckBox.setOnCheckedChangeListener({ buttonView, isChecked ->
            image?.metadata?.interventionRequired = isChecked
            image?.saveMetaData()
        })


        notesField = bind(R.id.notes)
        notesField.setText(image?.metadata?.notes ?: "")
        notesField.afterTextChanged { editable: Editable? ->
            image?.metadata?.notes = editable.toString()
            image?.saveMetaData()
        }

        coordinatesTextView = bind(R.id.coordinates_text_view)
        coordinatesTextView.text = image?.metadata?.coordinatesString()

        updateImage()
    }

    private fun updateImage() {
        image?.loadFullSize()?.then { bitmap ->
            imageView.post({
                imageView.setImageBitmap(bitmap)
            })

        }
    }

}
