package amal.global.amal;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by soroushkhanlou on 11/13/17.
 */

public class PhotoStorage {

    Context context;

    PhotoStorage(Context context) {
        this.context = context;
    }

    void savePhotoLocally() {

    }

    public ArrayList<Image> fetchImages() {
        final File dir = new File(context.getFilesDir() + "/images/");
        dir.mkdirs();
        File[] files = dir.listFiles();
        ArrayList<Image> images = new ArrayList<Image>();
        for (File file : files) {
            String filename = file.getName();
            String fileExtension = FilenameUtils.getExtension(filename);
            String IDString = FilenameUtils.removeExtension(filename);
            if (fileExtension.equals("jpg") || fileExtension.equals("jpeg")) {
                images.add(new Image(file.getAbsolutePath()));
            }
        }
        return images;
    }

}
