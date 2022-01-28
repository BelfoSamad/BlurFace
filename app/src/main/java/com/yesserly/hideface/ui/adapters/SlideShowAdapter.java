package com.yesserly.hideface.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.yesserly.hideface.R;
import com.yesserly.hideface.utils.Config;
import com.bumptech.glide.Glide;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class SlideShowAdapter extends BaseAdapter {
    private static final String TAG = "SlideShowAdapter";

    /***********************************************************************************************
     **************************************** Declarations
     */
    private Context context;

    /***********************************************************************************************
     **************************************** Constructor
     */
    public SlideShowAdapter(Context context) {
        this.context = context;
    }

    /***********************************************************************************************
     **************************************** Methods
     */
    @Override
    public int getCount() {
        return Config.slideImages.length;
    }

    @Override
    public Object getItem(int position) {
        return Config.slideImages[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).
                    inflate(R.layout.slide_item, parent, false);
        }

        int image = (int) getItem(position);
        Glide.with(context).load(image)
                .transform(new BlurTransformation(5, 4))
                .into((ImageView) convertView.findViewById(R.id.image));

        return convertView;
    }
}
