package amal.global.amal

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

sealed class GalleryItem
data class GalleryDateDivider(val dateString:String): GalleryItem()
data class GalleryPhoto(val localImage: LocalImage): GalleryItem()

class GalleryRecyclerViewHolder(val itemView: View): RecyclerView.ViewHolder(itemView) {
    //TODO implement view holder itemView.view.text =
    //some way to get the actual item into it

}

class GalleryRecyclerAdapter(private val context: Context): RecyclerView.Adapter<GalleryRecyclerViewHolder>() {
    companion object {
        const val TAG = "GallRecyclerAdapter"
    }

    private val galleryItems = mutableListOf<GalleryItem>()
    private var selectedImages = mutableListOf<Int>()

    init {
        var lastDate =  ""
        var images: List<LocalImage> = PhotoStorage(context).fetchImages()

        createGalleryItemList(images)
    }

    override fun getItemCount(): Int {
        return galleryItems.size
    }
    
    override fun getItemViewType(position: Int): Int {
        return if (galleryItems[position] is GalleryDateDivider) {
            1 //normally define constants
        } else {
            2
        }
        //
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryRecyclerViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: GalleryRecyclerViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    fun createGalleryItemList(imageList: List<LocalImage>) {
        images.forEach {
            if (it.localDateString != lastDate) {
                lastDate = it.localDateString ?: ""
                galleryItems.add(GalleryDateDivider(it.localDateString ?: "No Date"))
            }
            galleryItems.add(GalleryPhoto(it))
        }
    }

    fun reloadData() {
        images = PhotoStorage(context).fetchImages()
        createGalleryItemList(images)
        selectedImages = mutableListOf()
    }
}