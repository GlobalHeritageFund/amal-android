package global.amal.app

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView

class SubtitleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var title = view.bind<TextView>(R.id.titleTextView)
    var subtitle = view.bind<TextView>(R.id.subtitleTextView)
    var imageView = view.bind<ImageView>(R.id.imageView)
}
