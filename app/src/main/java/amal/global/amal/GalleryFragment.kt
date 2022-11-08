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
    //TODO need to also create adn implemnt GalleryAdapterDelegate?

    companion object {
        const val TAG = "Gallery Fragment"
    }

    var delegate: GalleryDelegate? = null

//    internal var imageAdapter: GalleryAdapter? = null
    lateinit var recyclerAdapter: GalleryRecyclerAdapter? = null
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
//        assessRecyclerView.layoutManager = GridLayoutManager(activity)
        //aove commented bc think doing it in xml
        assessRecyclerView.adapter = recyclerAdapter
        assessRecyclerView.emptyView = empty_gallery_view


        assessRecyclerView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            Log.d(TAG,"click listener on picture got called")
            val image = recyclerAdapter.galleryItems[position] as LocalImage
            delegate?.imageTapped(this, image)
            //TODO this is wrong - have issues with imagelist vs gallerylist
        }

        emptyView = bind(R.id.empty_gallery_view)

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

    fun updateData() {
        recyclerAdapter?.reloadData()
        recyclerAdapter?.notifyDataSetChanged()
    }

}

