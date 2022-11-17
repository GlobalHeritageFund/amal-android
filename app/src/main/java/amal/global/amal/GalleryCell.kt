package amal.global.amal

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout

class GalleryCell(context: Context?) : RelativeLayout(context) {

    init {
        LayoutInflater.from(context).inflate(R.layout.cell_gallery, this, true)
    }

}