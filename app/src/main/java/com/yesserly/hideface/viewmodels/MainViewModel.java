package com.yesserly.hideface.viewmodels;

import android.os.Environment;

import androidx.lifecycle.ViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainViewModel extends ViewModel {
    private static final String TAG = "MainViewModel";

    /***********************************************************************************************
     **************************************** Declarations
     */
    private String image_path;

    /***********************************************************************************************
     **************************************** Methods
     */
    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).toString()
                + File.separator + "BlurFace");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image_file = File.createTempFile(imageFileName, ".jpg", storageDir);
        image_path = image_file.getAbsolutePath();

        return image_file;
    }

    public String getImage_path() {
        return image_path;
    }
}