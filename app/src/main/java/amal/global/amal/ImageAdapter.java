package amal.global.amal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by soroushkhanlou on 11/14/17.
 */
class ImageAdapter extends BaseAdapter {

    private List<Image> images;

    private Context context;

    public ImageAdapter(Context c) {
        context = c;
        images = new PhotoStorage(context).fetchImages();
    }

    @Override
    public int getCount() {
        return images.size();
    }

    public Object getItem(int position) {
        return images.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(context);
        } else {
            imageView = (ImageView)convertView;
        }

        Image image = (Image)getItem(position);
        Bitmap fullBitmap = BitmapFactory.decodeFile(image.filePath);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(fullBitmap, 200, 200, true);
        imageView.setImageBitmap(resizedBitmap);
        return imageView;
    }

}
