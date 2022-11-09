package amal.global.amal

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_gallery.*

interface ChooseImagesFragmentDelegate {
    fun choseImages(fragment: ChooseImagesFragment, images: List<LocalImage>)
}

class ChooseImagesFragment: Fragment() {

    companion object {
        const val TAG = "Choose Fragment"
        private const val TYPE_DIVIDER = 1
    }
    lateinit var adapter: GalleryRecyclerAdapter

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
        assessRecyclerView.adapter = adapter
        assessRecyclerView.layoutManager = GridLayoutManager(activity,3).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (adapter!!.getItemViewType(position)){
                        TYPE_DIVIDER -> 3
                        else ->  1
                    }
                }
            }
        }
        assessRecyclerView.addOnItemClickListener(object: OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                adapter.toggleSelectionAt(position)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item!!.getItemId()) {
            R.id.menu_item_choose_images_next -> {
                delegate?.choseImages(this, adapter.selectedItems())
                return true
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
    }


}
