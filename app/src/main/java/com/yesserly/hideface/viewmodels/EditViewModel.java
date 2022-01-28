package com.yesserly.hideface.viewmodels;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.yesserly.hideface.pojo.Area;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditViewModel extends ViewModel {
    private static final String TAG = "EditViewModel";

    /***********************************************************************************************
     **************************************** Declarations
     */
    private MutableLiveData<List<Face>> detectedFaces;
    private boolean edited = false;
    private ArrayList<Area> selectedAreas = new ArrayList<>();

    /***********************************************************************************************
     **************************************** Methods
     */
    public int getCameraPhotoOrientation(String imagePath) {
        int rotate = 0;
        try {
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    public int getCameraPhotoOrientation(Context context, Uri imageUri) {
        int rotate = 0;
        try {
            ExifInterface exif;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                exif = new ExifInterface(context.getContentResolver().openInputStream(imageUri));
            } else {
                String wholeID = DocumentsContract.getDocumentId(imageUri);

                // Split at colon, use second item in the array
                String id = wholeID.split(":")[1];
                String[] column = {MediaStore.Images.Media.DATA};

                // where id is equal to
                String sel = MediaStore.Images.Media._ID + "=?";
                Cursor cursor = context.getContentResolver().
                        query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                column, sel, new String[]{id}, null);

                String filePath = "";
                int columnIndex = cursor.getColumnIndex(column[0]);
                if (cursor.moveToFirst()) {
                    filePath = cursor.getString(columnIndex);
                }
                cursor.close();
                exif = new ExifInterface(filePath);
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return rotate;
    }

    public MutableLiveData<List<Face>> getDetectedFaces() {
        if (detectedFaces == null)
            detectedFaces = new MutableLiveData<>();
        return detectedFaces;
    }

    public void DetectFaces(Bitmap bitmap, int rotation) {
        //Prepare Options
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .build();

        //Prepare Image
        InputImage image = InputImage.fromBitmap(bitmap, rotation);

        //Get FaceDetector Instance
        FaceDetector detector = FaceDetection.getClient(options);

        //Get Results
        Task<List<Face>> result = detector.process(image)
                .addOnSuccessListener(
                        faces -> detectedFaces.setValue(faces))
                .addOnFailureListener(
                        e -> detectedFaces.setValue(null));
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public void addToSelected(Area ar) {
        selectedAreas.add(ar);
    }

    public void removeFromSelected(Area ar) {
        selectedAreas.remove(ar);
    }

    public ArrayList<Area> getSelectedAreas() {
        return selectedAreas;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}