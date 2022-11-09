package amal.global.amal

import amal.global.amal.GalleryRecyclerAdapter.Companion.TYPE_DIVIDER
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_report_detail.*
import java.nio.file.Files.delete

interface GalleryDelegate {
    fun imageTapped(fragment: GalleryFragment, image: LocalImage)
    fun importButtonTapped(fragment: GalleryFragment)
}

class GalleryFragment : Fragment() {
    //TODO will need to make empty state logic reactive to change in data, but leave for now bc will be affected by other changes

    companion object {
        const val TAG = "Gallery Fragment"
    }

    var delegate: GalleryDelegate? = null

    lateinit var recyclerAdapter: GalleryRecyclerAdapter
    lateinit var assessRecyclerView: RecyclerView
    lateinit var emptyView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyclerAdapter = GalleryRecyclerAdapter(requireActivity().applicationContext)
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
        assessRecyclerView = bind(R.id.assessRecyclerView)
        emptyView = bind(R.id.empty_gallery_view)
        assessRecyclerView.adapter = recyclerAdapter
        assessRecyclerView.layoutManager = GridLayoutManager(activity,3).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int)= when (recyclerAdapter.getItemViewType(position)){
                    TYPE_DIVIDER -> 3
                    else ->  1
                }
            }
        }

        if (recyclerAdapter.galleryItems.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            assessRecyclerView.visibility = View.GONE
        } else {
            assessRecyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
        }

        assessRecyclerView.addOnItemClickListener(object: OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                if (recyclerAdapter!!.getItemViewType(position) == TYPE_DIVIDER) return
                assessGalleryClickHandle(position)
            }
        })
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

    fun assessGalleryClickHandle(position: Int) {
        val galleryPhoto = recyclerAdapter!!.galleryItems[position]  as GalleryItem.GalleryPhoto
        val image = galleryPhoto.photoToShow as LocalImage
        delegate?.imageTapped(this, image)
    }

    fun updateData() {
        recyclerAdapter?.reloadData()
        recyclerAdapter?.notifyDataSetChanged()
    }

}


