package amal.global.amal

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import kotlinx.android.synthetic.main.cell_gallery.view.*
import java.lang.reflect.Type
import java.util.concurrent.Semaphore

public class GalleryAdapter(private val context: Context) : BaseAdapter() {

    private val images: List<Image> = PhotoStorage(context).fetchImages()

    private val semaphore = Semaphore(3)

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

        (image as? LocalImage)?.let {
            GlideApp.with(context)
                    .load(it.file)
                    .centerCrop()
                    .into(galleryCell.contentImageView)
        }
//        semaphore.acquirePromise()
//                .flatMap {
//                    image.loadThumbnail(context!!)
//                }
//                .then({ thumbnail ->
//                    galleryCell.post {
//                        galleryCell.contentImageView.setImageBitmap(thumbnail)
//                    }
//                    semaphore.release()
//                })

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

}

