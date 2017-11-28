package amal.global.amal

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView

interface ChooseImagesFragmentDelegate {
    fun choseImages(fragment: ChooseImagesFragment, images: List<Image>)
}

class ChooseImagesFragment: Fragment() {

    lateinit var gridView: GridView

    lateinit var adapter: GalleryAdapter

    var delegate: ChooseImagesFragmentDelegate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = GalleryAdapter(activity)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gridView = bind(R.id.gallery_gridview)

        gridView.adapter = adapter

        gridView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            adapter.toggleSelectionAt(position)
        }
    }

}