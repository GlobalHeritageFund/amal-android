package amal.global.amal

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.cell_gallery.view.*

sealed class GalleryItem {
    data class GalleryDateDivider(val photoGroupDate: String): GalleryItem()
    data class GalleryPhoto(val photoToShow: LocalImage): GalleryItem()
}

class GalleryRecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    private fun bindPhoto(item: GalleryItem.GalleryPhoto) {
        item.photoToShow.load(itemView.context).centerCrop().into(itemView.contentImageView)
    }

    private fun bindDivider(item: GalleryItem.GalleryDateDivider) {
        itemView.findViewById<TextView>(R.id.date_text)?.text = item.photoGroupDate
    }

    fun bind(galleryItem: GalleryItem) {
        when (galleryItem) {
            is GalleryItem.GalleryPhoto -> bindPhoto(galleryItem)
            is GalleryItem.GalleryDateDivider -> bindDivider(galleryItem)
        }
    }
}

class GalleryRecyclerAdapter(private val context: Context): RecyclerView.Adapter<GalleryRecyclerViewHolder>() {
    companion object {
        const val TAG = "GallRecyclerAdapter"
        const val TYPE_PHOTO = 0
        const val TYPE_DIVIDER = 1
    }

    val galleryItems = mutableListOf<GalleryItem>()
    private var selectedImages = mutableListOf<Int>()

    init {
        val images = PhotoStorage(context).fetchImagesSortedByDateDesc()
        createGalleryItemList(images)
    }

    override fun getItemCount() = galleryItems.size

    override fun getItemViewType(position: Int) = when (galleryItems[position]) {
        is GalleryItem.GalleryPhoto -> TYPE_PHOTO
        is GalleryItem.GalleryDateDivider -> TYPE_DIVIDER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryRecyclerViewHolder {
        val layout = when (viewType) {
            TYPE_PHOTO -> R.layout.cell_gallery
            TYPE_DIVIDER -> R.layout.date_divider
            else -> throw IllegalArgumentException("Invalid view type")
        }

        val view = LayoutInflater
                .from(parent.context)
                .inflate(layout, parent, false)

        return GalleryRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryRecyclerViewHolder, position: Int) {
        holder.bind(galleryItems[position])
        if (holder.itemViewType == TYPE_PHOTO) {
            holder.itemView.selectionStateImageView.visibility = if (selectedImages.contains(position)) View.VISIBLE else View.INVISIBLE
        }

    }

    private fun createGalleryItemList(imageList: List<LocalImage>) {
        var lastDate = ""
        imageList.forEach {
            if (it.localDateString != lastDate) {
                lastDate = it.localDateString ?: ""
                galleryItems.add(GalleryItem.GalleryDateDivider(it.localDateString ?: "No Date"))
            }
            galleryItems.add(GalleryItem.GalleryPhoto(it))
        }
    }

    //not sure if should send this through adapter first or just call directly through galleryfragment
    //keep here for now until figure out how will implement the select function
    fun deleteImage(imagePath: String, settingsPath: String) {
        Log.d(GalleryRecyclerAdapter.TAG, "deleteImage was called")
        PhotoStorage(context).deleteImage(imagePath, settingsPath)
        reloadData()
        // Todo Use notifyItemRemoved() instead
        notifyDataSetChanged()
    }

    fun getImage(position: Int): Any {
        val selectedGalleryItem = galleryItems[position] as GalleryItem.GalleryPhoto
        return selectedGalleryItem.photoToShow
    }

    fun selectedItems(): List<LocalImage> {
        return selectedImages.sorted().mapNotNull { getImage(it) as? LocalImage }
    }

    fun toggleSelectionAt(position: Int) {
        if (selectedImages.contains(position)) {
            selectedImages.remove(position)
        } else {
            selectedImages.add(position)
        }
        notifyItemChanged(position)
    }

    fun reloadData() {
        createGalleryItemList(PhotoStorage(context).fetchImagesSortedByDateDesc())
        selectedImages = mutableListOf()
    }
}
