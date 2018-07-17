package amal.global.amal

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.cell_gallery.view.*

public class GalleryAdapter(private val context: Context) : BaseAdapter() {

    private var images: List<Image> = PhotoStorage(context).fetchImages()

    private var selectedImages = mutableListOf<Int>()

    override fun getCount(): Int {
        return images.size
    }

    override fun getItem(position: Int): Any {
        return images[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val galleryCell = (convertView as? GalleryCell) ?: GalleryCell(context)

        val image = getItem(position) as Image

        image.load(context).centerCrop().into(galleryCell.contentImageView)

        galleryCell.selectionStateImageView.visibility = if (selectedImages.contains(position)) View.VISIBLE else View.INVISIBLE
        return galleryCell
    }

    fun selectedItems(): List<LocalImage> {
        return selectedImages.sorted().mapNotNull { getItem(it) as? LocalImage }
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
        images = PhotoStorage(context).fetchImages()
        selectedImages = mutableListOf()
    }

}

