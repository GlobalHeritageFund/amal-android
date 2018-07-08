package amal.global.amal

import android.content.Context
import android.graphics.Bitmap
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import java.util.concurrent.Semaphore

class ReportDetailAdapter(val context: Context, var report: Report) : RecyclerView.Adapter<SubtitleViewHolder>() {

    val semaphore = Semaphore(3)

    init {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtitleViewHolder {
        val itemView = parent.inflate(R.layout.subtitle_row, false)

        return SubtitleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SubtitleViewHolder, position: Int) {
        val image = report.images[position]

        holder.title.text = image.metadata.name
        holder.subtitle.text = image.metadata.coordinatesString()

        semaphore.acquirePromise()
                .flatMap {
                    return@flatMap image.loadThumbnail(context!!)
                }
                .then { thumbnail: Bitmap ->
                    holder.imageView.post({
                        holder.imageView.setImageBitmap(thumbnail)
                    })
                    semaphore.release()
                }
    }

    override fun getItemCount(): Int {
        return report.images.size
    }
}
