package amal.global.amal

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.AdapterView
import kotlinx.android.synthetic.main.fragment_gallery.*

interface GalleryDelegate {
    fun imageTapped(fragment: GalleryFragment, image: LocalImage)
    fun importButtonTapped(fragment: GalleryFragment)
}

class GalleryFragment : Fragment() {

    var delegate: GalleryDelegate? = null

    internal var imageAdapter: GalleryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageAdapter = GalleryAdapter(requireActivity().applicationContext)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_gallery, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item!!.getItemId()) {
            R.id.menu_item_import -> {
                delegate?.importButtonTapped(this)
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }

    fun updateData() {
        imageAdapter?.reloadData()
        imageAdapter?.notifyDataSetChanged()
    }

}

