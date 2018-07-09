package amal.global.amal

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import kotlinx.android.synthetic.main.fragment_gallery.*

public interface GalleryDelegate {
    public fun imageTapped(fragment: GalleryFragment, image: LocalImage)
}

class GalleryFragment : Fragment() {

    var delegate: GalleryDelegate? = null

    internal var imageAdapter: GalleryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageAdapter = GalleryAdapter(activity!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.setTitle(R.string.title_assess)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gridView.adapter = imageAdapter

        gridView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val image = imageAdapter?.getItem(position) as LocalImage
            delegate?.imageTapped(this, image)
        }
    }
}

