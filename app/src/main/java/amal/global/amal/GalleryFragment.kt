package amal.global.amal

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.AdapterView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.android.synthetic.main.fragment_report_detail.*
import java.nio.file.Files.delete

interface GalleryDelegate {
    fun imageTapped(fragment: GalleryFragment, image: LocalImage)
    fun importButtonTapped(fragment: GalleryFragment)
}

class GalleryFragment : Fragment() {
    //TODO need to also create and implemnt GalleryAdapterDelegate? foe empty state

    companion object {
        const val TAG = "Gallery Fragment"
        private const val TYPE_DIVIDER = 1
    }

    var delegate: GalleryDelegate? = null

//    internal var imageAdapter: GalleryAdapter? = null
    var recyclerAdapter: GalleryRecyclerAdapter? = null
    lateinit var assessRecyclerView: RecyclerView
    lateinit var emptyView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        imageAdapter = GalleryAdapter(requireActivity().applicationContext)
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
        assessRecyclerView = bind(R.id.assess_recycler_view)
        assessRecyclerView.adapter = recyclerAdapter
        assessRecyclerView.layoutManager = GridLayoutManager(activity,3).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return when (recyclerAdapter!!.getItemViewType(position)){
                        TYPE_DIVIDER -> 3
                        else ->  1
                    }
                }
            }
        }

        assessRecyclerView.addOnItemClickListener(object: OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                if (recyclerAdapter!!.getItemViewType(position) == TYPE_DIVIDER) return
                assessGalleryClickHandle(position)
            }
        })

        //aove commented bc think doing it in xml
//        assessRecyclerView.emptyView = empty_gallery_view


//        assessRecyclerView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
//            Log.d(TAG,"click listener on picture got called")
//            val image = recyclerAdapter.galleryItems[position] as LocalImage
//            delegate?.imageTapped(this, image)
//            //TODO this is wrong - have issues with imagelist vs gallerylist
//        }

//        emptyView = bind(R.id.empty_gallery_view)

//        gridView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
//            Log.d(TAG,"click listener on picture got called")
//            val image = imageAdapter?.getItem(position) as LocalImage
//            imageAdapter?.deleteImage(image.filePath, image.settingsPath)
//        }
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

//    override fun photosFound() {
//        Log.d(ReportsFragment.TAG, "reports found called")
//        listView.visibility = View.VISIBLE
//        emptyView.visibility = View.GONE
//
//    }
//
//    override fun noPhotosFound() {
//        Log.d(ReportsFragment.TAG, "reports not found called")
//        emptyView.visibility = View.VISIBLE
//        listView.visibility = View.GONE
//    }

    fun assessGalleryClickHandle(position: Int) {
        val galleryPhoto = recyclerAdapter!!.galleryItems[position]  as GalleryItem.GalleryPhoto
        val image = galleryPhoto.photoToShow as LocalImage
        delegate?.imageTapped(this, image)
    }

    fun updateData() {
        //TODO this is broken just to be able to test other features first
//        recyclerAdapter?.reloadData()
        recyclerAdapter?.notifyDataSetChanged()
    }

}

