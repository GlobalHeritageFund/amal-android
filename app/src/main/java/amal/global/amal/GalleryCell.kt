package amal.global.amal

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.cell_gallery.view.*

class GalleryCell(context: Context?) : RelativeLayout(context) {

    init {
        LayoutInflater.from(context).inflate(R.layout.cell_gallery, this, true)
    }

}