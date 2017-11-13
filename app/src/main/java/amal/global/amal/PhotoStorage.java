package amal.global.amal;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by soroushkhanlou on 11/13/17.
 */

public class PhotoStorage {

    Context context;

    PhotoStorage(Context context) {
        this.context = context;
    }

    void savePhotoLocally(byte[] bytes) {
        final File dir = new File(context.getFilesDir() + "/images/");
        dir.mkdirs();
        int maxImageID = -1;
        for (File file : dir.listFiles()) {
            String filename = file.getName();
            String IDString = FilenameUtils.removeExtension(filename);
            int id = Integer.parseInt(IDString);
            if (id > maxImageID) {
                maxImageID = id;
            }
        }
        final File file = new File(dir + String.format("/%d.jpg", maxImageID+1));
        OutputStream output = null;
        try {
            save(bytes, file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save(byte[] bytes, File file) throws IOException {
        OutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(bytes);
        } finally {
            if (null != output) {
                output.close();
            }
        }
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
