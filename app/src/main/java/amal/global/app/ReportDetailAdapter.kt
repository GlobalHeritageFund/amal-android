package global.amal.app

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup

class ReportDetailAdapter(val context: Context, var report: Report) : RecyclerView.Adapter<SubtitleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtitleViewHolder {
        val itemView = parent.inflate(R.layout.subtitle_row, false)

        return SubtitleViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SubtitleViewHolder, position: Int) {
        val image = report.images[position]

        holder.title.text = image.metadata.name
        holder.subtitle.text = image.metadata.coordinatesString()

        image.load(context).centerCrop().into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return report.images.size
    }
}
