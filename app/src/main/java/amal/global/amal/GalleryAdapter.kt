package amal.global.amal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.ContactsContract
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import java.util.concurrent.Semaphore
import kotlin.concurrent.thread
import kotlin.coroutines.experimental.suspendCoroutine

internal class GalleryAdapter(private val context: Context) : BaseAdapter() {

    private val images: List<Image>

    private val semaphore = Semaphore(3)

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
        val imageView = (convertView as? ImageView) ?: ImageView(context)

        val image = getItem(position) as Image

        thread {
            semaphore.acquire()
            val fullBitmap = BitmapFactory.decodeFile(image.filePath)
            val resizedBitmap = Bitmap.createScaledBitmap(fullBitmap, 200, 200, true)
            imageView.post {
                imageView.setImageBitmap(resizedBitmap)
                semaphore.release()
            }
        }

        return imageView
    }

}
