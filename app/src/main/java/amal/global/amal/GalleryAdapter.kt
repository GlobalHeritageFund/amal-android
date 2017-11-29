package amal.global.amal

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import java.io.File
import java.util.concurrent.Semaphore

public class GalleryAdapter(private val context: Context) : BaseAdapter() {

    private val images: List<Image>

    private val semaphore = Semaphore(3)

    var selectedImages = mutableListOf<Int>()

    init {
        images = PhotoStorage(context).fetchImages()
    }

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
        val imageView = (convertView as? GalleryCell) ?: GalleryCell(context)

        val image = getItem(position) as Image

        semaphore.acquire()
        File(image.filePath)
                .decodeBitmap()
                .flatMap<Bitmap>({ fullBitmap ->
                    fullBitmap.scale(200, 200, true)
                })
                .then({ scaledBitmap ->
                    imageView.post {
                        imageView.contentImageView.setImageBitmap(scaledBitmap)
                    }
                    semaphore.release()
                })

        imageView.selectionStateImageView.visibility = if (selectedImages.contains(position)) View.VISIBLE else View.INVISIBLE
        return imageView
    }

    fun selectedItems(): List<Image> {
        return selectedImages.sorted().map { getItem(it) as Image }
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

