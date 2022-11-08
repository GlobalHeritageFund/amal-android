package amal.global.amal

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.GridView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_gallery.*

interface ChooseImagesFragmentDelegate {
    fun choseImages(fragment: ChooseImagesFragment, images: List<LocalImage>)
}

class ChooseImagesFragment: Fragment() {

    lateinit var adapter: GalleryRecyclerAdapter
    lateinit var assessRecyclerView: RecyclerView

    var delegate: ChooseImagesFragmentDelegate? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = GalleryRecyclerAdapter(requireActivity().applicationContext)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_choose_images, menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        assessRecyclerView = bind(R.id.assess_recycler_view)
        assessRecyclerView.adapter = adapter

//        assessRecyclerView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
//            adapter.toggleSelectionAt(position)
//        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item!!.getItemId()) {
            R.id.menu_item_choose_images_next -> {
//                delegate?.choseImages(this, adapter.selectedItems())
                return true
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
    }


}
