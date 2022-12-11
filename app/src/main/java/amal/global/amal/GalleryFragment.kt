package amal.global.amal

import amal.global.amal.GalleryRecyclerAdapter.Companion.TYPE_DIVIDER
import amal.global.amal.databinding.FragmentGalleryBinding
import android.os.Build.VERSION_CODES.S
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import java.nio.file.Files.delete

interface GalleryDelegate {
//    fun imageTapped(fragment: GalleryFragment, image: LocalImage)
    fun importButtonTapped(fragment: GalleryFragment)
    fun choseImagesToAssess(fragment: GalleryFragment, images: List<LocalImage>)
}

class GalleryFragment : Fragment() {

    companion object {
        const val TAG = "Gallery Fragment"
    }

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    var delegate: GalleryDelegate? = null

    lateinit var recyclerAdapter: GalleryRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recyclerAdapter = GalleryRecyclerAdapter(requireActivity().applicationContext)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.setTitle(R.string.title_assess)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.assessRecyclerView.adapter = recyclerAdapter
        binding.assessRecyclerView.layoutManager = GridLayoutManager(activity,3).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int)= when (recyclerAdapter.getItemViewType(position)){
                    TYPE_DIVIDER -> 3
                    else ->  1
                }
            }
        }

        if (recyclerAdapter.galleryItems.isEmpty()) {
            binding.emptyGalleryView.visibility = View.VISIBLE
            binding.assessRecyclerView.visibility = View.GONE
        } else {
            binding.assessRecyclerView.visibility = View.VISIBLE
            binding.emptyGalleryView.visibility = View.GONE
        }

        binding.assessRecyclerView.addOnItemClickListener(object: OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                if (recyclerAdapter!!.getItemViewType(position) == TYPE_DIVIDER) return
//                assessGalleryClickHandle(position)
                recyclerAdapter.toggleSelectionAt(position)
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
            R.id.menu_item_delete -> {
                handleMultiDeleteClick()
                true
            }
            R.id.menu_item_assess -> {
                handleMultiAssessClick()
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //currently not being called bc switched clickhandler to toggleBoxes instead
//    fun assessGalleryClickHandle(position: Int) {
//        val galleryPhoto = recyclerAdapter!!.galleryItems[position]  as GalleryItem.GalleryPhoto
//        val image = galleryPhoto.photoToShow as LocalImage
//        delegate?.imageTapped(this, image)
//    }

    fun handleMultiDeleteClick() {
        if (recyclerAdapter.selectedItems().size > 0) {
            createDeleteAlert()
//            recyclerAdapter.deleteSelectedImages()
//            updateData()
        } else {
            Snackbar.make(binding.root,"Must select at least one item to delete",Snackbar.LENGTH_LONG).show()
        }
    }

    private fun createDeleteAlert() {
        val builder = AlertDialog.Builder(requireContext());
        builder.setMessage("This will permanently delete your selected images. Do you want to continue?");
        builder.setPositiveButton(R.string.delete) { dialog, which ->
            recyclerAdapter.deleteSelectedImages()
//            updateData()
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") {dialog, which -> dialog.dismiss()}
        builder.show()
    }

    fun handleMultiAssessClick() {
        if (recyclerAdapter.selectedItems().size > 0) delegate?.choseImagesToAssess(this, recyclerAdapter.selectedItems())
        else Snackbar.make(binding.root,"Must select at least one item to assess",Snackbar.LENGTH_LONG).show()

    }

    fun updateData() {
        recyclerAdapter?.reloadData()
        recyclerAdapter?.notifyDataSetChanged()
    }

}


