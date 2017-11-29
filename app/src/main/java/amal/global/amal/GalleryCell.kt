package amal.global.amal

import android.content.Context
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout

class GalleryCell(context: Context?) : RelativeLayout(context) {

    lateinit var contentImageView: ImageView
    lateinit var selectionStateImageView: ImageView

    init {
        LayoutInflater.from(context).inflate(R.layout.cell_gallery, this, true)
        contentImageView = bind(R.id.gallery_cell_content_image_view)
        selectionStateImageView = bind(R.id.gallery_cell_selection_state)
    }
}