package com.yesserly.hideface.utils;

import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import htk.lib.areasimage.ClickableArea;
import htk.lib.areasimage.ImageUtils;
import htk.lib.areasimage.OnClickableAreaClickedListener;
import htk.lib.areasimage.PixelPosition;
import htk.lib.photoview.PhotoViewAttacher;

public class ClickableAreasImage implements PhotoViewAttacher.OnPhotoTapListener {
    private static final String TAG = "ClickableAreasImage";

    private PhotoViewAttacher attacher;
    private OnClickableAreaClickedListener listener;
    private List<ClickableArea> clickableAreas;
    private int imageWidthInPx;
    private int imageHeightInPx;

    public ClickableAreasImage(ImageView imageView, OnClickableAreaClickedListener listener) {
        this.attacher = new PhotoViewAttacher(imageView);
        this.init(listener);
    }

    private void init(OnClickableAreaClickedListener listener) {
        this.listener = listener;
        this.getImageDimensions(this.attacher.getImageView());
        this.attacher.setOnPhotoTapListener(this);
    }

    private void getImageDimensions(ImageView imageView) {
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        this.imageWidthInPx = drawable.getBitmap().getWidth();
        this.imageHeightInPx = drawable.getBitmap().getHeight();
    }

    public void onPhotoTap(View view, float positionXOnDrawable, float positionYOnDrawable, float positionXOnScreen, float positionYOnScreen) {
        PixelPosition pixel = ImageUtils.getPixelPosition(positionXOnDrawable, positionYOnDrawable, this.imageWidthInPx, this.imageHeightInPx);
        List<ClickableArea> clickableAreas = this.getClickAbleAreas(pixel.getX(), pixel.getY());
        Iterator var8 = clickableAreas.iterator();

        while (var8.hasNext()) {
            ClickableArea ca = (ClickableArea) var8.next();
            this.listener.onClickableAreaTouched(ca.getItem(), positionXOnDrawable, positionYOnDrawable, positionXOnScreen, positionYOnScreen);
        }

    }

    private List<ClickableArea> getClickAbleAreas(int x, int y) {
        List<ClickableArea> clickableAreas = new ArrayList();
        Iterator var4 = this.getClickableAreas().iterator();

        while (var4.hasNext()) {
            ClickableArea ca = (ClickableArea) var4.next();
            if (this.isBetween(ca.getX(), ca.getX() + ca.getW(), x) && this.isBetween(ca.getY(), ca.getY() + ca.getH(), y)) {
                clickableAreas.add(ca);
            }
        }

        return clickableAreas;
    }

    public void setEnableScalePicture(boolean isEnableScale) {
        this.attacher.setEnableScale(isEnableScale);
    }

    private boolean isBetween(int start, int end, int actual) {
        return start <= actual && actual <= end;
    }

    public void setClickableAreas(List<ClickableArea> clickableAreas) {
        this.clickableAreas = clickableAreas;
    }

    public List<ClickableArea> getClickableAreas() {
        return this.clickableAreas;
    }
}
