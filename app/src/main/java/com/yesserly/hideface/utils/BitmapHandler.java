package com.yesserly.hideface.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import com.yesserly.hideface.pojo.Area;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BitmapHandler {
    private static final String TAG = "BitmapHandler";

    /***********************************************************************************************
     **************************************** Declarations
     */
    private Bitmap source;
    private Bitmap area;
    private Bitmap stickered_bitmap = null;
    private Context context;
    private Uri fileUri;
    private Paint myPaint;

    /***********************************************************************************************
     **************************************** Constructor
     */
    public BitmapHandler(Context context) {
        this.context = context;

        //Create Areas Paint Style
        myPaint = new Paint();
        myPaint.setStyle(Paint.Style.STROKE);
        myPaint.setStrokeWidth(10);
    }

    /***********************************************************************************************
     **************************************** Methods
     */
    public Bitmap createBitmap(String type, String path, int rotation) {
        Bitmap raw_source = null;
        switch (type) {
            case "camera":
                raw_source = BitmapFactory.decodeFile(path);
                break;
            case "gallery":
                try {
                    Uri uri = Uri.parse(path);
                    raw_source = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        source = Bitmap.createBitmap(raw_source, 0, 0, raw_source.getWidth(), raw_source.getHeight(),
                matrix, true);
        return source;
    }

    public Bitmap createEmptyBitmap(int width, int height) {
        area = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        return area;
    }

    public Bitmap addAreas(ArrayList<Rect> rects) {

        //Set Paint Color
        myPaint.setColor(Color.rgb(255, 0, 0));

        //Create Canvas
        Canvas areaCanvas = new Canvas(area);

        //Draw Rects
        for (Rect rect :
                rects) {
            areaCanvas.drawRect(rect, myPaint);
        }

        return area;
    }

    public Bitmap selectArea(Rect rect) {
        myPaint.setColor(Color.rgb(0, 255, 0));
        Canvas tempCanvas = new Canvas(area);
        tempCanvas.drawRect(rect, myPaint);

        return area;
    }

    public Bitmap unSelectArea(Rect rect) {
        myPaint.setColor(Color.rgb(255, 0, 0));
        Canvas tempCanvas = new Canvas(area);
        tempCanvas.drawRect(rect, myPaint);

        return area;
    }

    public Bitmap applyStickers(Bitmap sticker, ArrayList<Area> areas) {
        //Create Destination
        if (stickered_bitmap == null)
            stickered_bitmap = source.copy(Bitmap.Config.ARGB_8888, true);

        if (areas.isEmpty())
            return null;
        else {
            Canvas c_src = new Canvas(stickered_bitmap);

            //Paint for Masking
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

            for (Area ar :
                    areas) {
                Rect rect = ar.getRect();

                //Clear already existing sticker
                if (ar.isStickered())
                    c_src.drawBitmap(Bitmap.createScaledBitmap(Bitmap.createBitmap(source, rect.left, rect.top, rect.width(), rect.height()),
                            rect.width(), rect.height(), false),
                            rect.left, rect.top, paint);


                //Draw Sticker in Source (+ Scale Sticker)
                c_src.drawBitmap(Bitmap.createScaledBitmap(sticker, rect.width(), rect.height(), false),
                        rect.left, rect.top, paint);
                ar.setStickered(true);
            }
        }

        return stickered_bitmap;
    }

    public Bitmap clearStickers() {
        stickered_bitmap = source.copy(Bitmap.Config.ARGB_8888, true);
        return stickered_bitmap;
    }

    public Bitmap getSource() {
        return source;
    }

    public void saveBitmap() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_" + timeStamp + "_" + "Edited";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).toString()
                + File.separator + "HideFace");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File file = new File(storageDir, imageFileName + ".png");
        try (FileOutputStream out = new FileOutputStream(file)) {
            stickered_bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileUri = FileProvider.getUriForFile(context, "com.yesserly.hideface.fileprovider", file);
    }

    public Uri getFileUri() {
        if (fileUri == null)
            saveBitmap();
        return fileUri;
    }
}
