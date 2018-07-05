package amal.global.amal

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

class SubtitleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var title = view.bind<TextView>(R.id.titleTextView)
    var subtitle = view.bind<TextView>(R.id.subtitleTextView)
    var imageView = view.bind<ImageView>(R.id.imageView)
}
