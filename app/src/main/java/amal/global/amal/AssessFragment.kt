package amal.global.amal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView

import java.io.File
import java.util.ArrayList


class AssessFragment : Fragment() {

    internal var imageAdapter: ImageAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageAdapter = ImageAdapter(activity)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_assess, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gridview = getView()?.findViewById<View>(R.id.gridview) as GridView
        gridview.adapter = imageAdapter

        gridview.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val image = imageAdapter?.getItem(position) as Image
            Log.d("asf", image.filePath)
        }

    }
}

