package global.amal.app

import global.amal.app.GalleryRecyclerAdapter.Companion.TYPE_DIVIDER
import global.amal.app.databinding.FragmentGalleryBinding
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.GridLayoutManager

interface ChooseImagesFragmentDelegate {
    fun choseImages(fragment: ChooseImagesFragment, images: List<LocalImage>)
}

class ChooseImagesFragment: Fragment() {

    companion object {
        const val TAG = "Choose Fragment"
    }

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    lateinit var adapter: GalleryRecyclerAdapter

    var delegate: ChooseImagesFragmentDelegate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = GalleryRecyclerAdapter(requireActivity().applicationContext)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setHasOptionsMenu(true)
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_choose_images, menu)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.assessRecyclerView.adapter = adapter
        binding.assessRecyclerView.layoutManager = GridLayoutManager(activity,3).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int) = when (adapter.getItemViewType(position)) {
                    //type divider set to take up all 3 columns so spans entire width while pics take 1/3 screen
                    TYPE_DIVIDER -> 3
                    else ->  1
                }
            }
        }
        binding.assessRecyclerView.addOnItemClickListener(object: OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                adapter.toggleSelectionAt(position)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_item_choose_images_next -> {
                delegate?.choseImages(this, adapter.selectedItems())
                return true
            }
            else ->
                return super.onOptionsItemSelected(item)
        }
    }
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onDetach() {
        delegate = null
        super.onDetach()
    }
}

