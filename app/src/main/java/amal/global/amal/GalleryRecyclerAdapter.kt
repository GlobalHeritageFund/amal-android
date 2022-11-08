package amal.global.amal

import android.content.Context
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
        const val TAG = "Gallery Recycler Adapter"
    }

    private val galleryItems: List<GalleryItem>

    init {
        var images: List<LocalImage> = PhotoStorage(context).fetchImages()
        //key is date value is list of images
        //entries takes dictionary to a list
        //then have to sort by date again bc might have lost date ordering by how dictionary stored
        galleryItems = images.groupBy { it.localDateString ?: "No Date" }
                .entries
                .toList()
                .sortedByDescending { it.key }
                .flatMap {
                    val itemsList = mutableListOf<GalleryItem>()
                    itemsList.add(GalleryDateDivider(it.key))
                    itemsList.addAll(it.value.map { photo -> GalleryPhoto(photo) })
                    return@flatMap itemsList
                }
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
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

}