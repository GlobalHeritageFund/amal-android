package amal.global.amal

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView

public interface GalleryDelegate {
    public fun imageTapped(fragment: GalleryFragment, image: Image)
}

class GalleryFragment : Fragment() {

    var delegate: GalleryDelegate? = null

    internal var imageAdapter: GalleryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageAdapter = GalleryAdapter(activity)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity.setTitle(R.string.title_assess)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gridview = getView()?.findViewById<GridView>(R.id.gallery_gridview)
        gridview?.adapter = imageAdapter

        gridview?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val image = imageAdapter?.getItem(position) as Image
            delegate?.imageTapped(this, image)
        }
    }
}

