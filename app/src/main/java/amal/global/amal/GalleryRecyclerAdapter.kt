package amal.global.amal

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.cell_gallery.view.*
import java.util.Collections.addAll

sealed class GalleryItem {
    data class GalleryDateDivider(val photoGroupDate: String): GalleryItem()
    data class GalleryPhoto(val photoToShow: LocalImage): GalleryItem()
}

class GalleryRecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    private fun bindPhoto(item: GalleryItem.GalleryPhoto) {
        item.photoToShow.load(itemView.context).centerCrop().into(itemView.content_image_view)
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
        private const val TYPE_PHOTO = 0
        private const val TYPE_DIVIDER = 1
    }

    val galleryItems = mutableListOf<GalleryItem>()
    private var selectedImages = mutableListOf<Int>()
    var lastDate =  ""

    init {
        var images: List<LocalImage> = PhotoStorage(context).fetchImagesSortedByDateDesc()
        createGalleryItemList(images)
    }

    override fun getItemCount(): Int {
        return galleryItems.size
    }

    override fun getItemViewType(position: Int): Int {
        return when (galleryItems[position]) {
            is GalleryItem.GalleryPhoto -> TYPE_PHOTO
            else -> TYPE_DIVIDER
        }
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

    fun createGalleryItemList(imageList: List<LocalImage>) {
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
        Log.d(GalleryAdapter.TAG, "deleteImage was called")
        PhotoStorage(context).deleteImage(imagePath, settingsPath)
        reloadData()
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
        notifyDataSetChanged()
    }

    fun reloadData() {
        createGalleryItemList(PhotoStorage(context).fetchImagesSortedByDateDesc())
        selectedImages = mutableListOf()
    }
}