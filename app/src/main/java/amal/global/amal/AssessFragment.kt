package amal.global.amal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

class AssessFragment : Fragment() {

    var image: Image? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_assess, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fullBitmap = BitmapFactory.decodeFile(image!!.filePath)
        val resizedBitmap = Bitmap.createScaledBitmap(fullBitmap, 200, 200, true)

        val imageView = getView()?.findViewById<ImageView>(R.id.image_view)
        imageView?.setImageBitmap(resizedBitmap)

    }

}
