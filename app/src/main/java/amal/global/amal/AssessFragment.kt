package amal.global.amal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView

class AssessFragment : Fragment() {

    var image: Image? = null

    lateinit var imageView: ImageView
    lateinit var nameField: EditText

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_assess, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageView = bind(R.id.image_view)
        nameField = bind(R.id.name_field)

        nameField.setText(image?.metadata?.name ?: "")

        Log.d("Asdf", "setting up listener")

        nameField.afterTextChanged { editable: Editable? ->
            image?.metadata?.name = editable.toString()
            image?.saveMetaData()
        }

        updateImage()
    }

    private fun updateImage() {
        val fullBitmap = BitmapFactory.decodeFile(image!!.filePath)
        val resizedBitmap = Bitmap.createScaledBitmap(fullBitmap, 200, 200, true)

        imageView.setImageBitmap(resizedBitmap)
    }

}
