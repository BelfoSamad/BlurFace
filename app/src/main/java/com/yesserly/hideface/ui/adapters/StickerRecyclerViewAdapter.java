package com.yesserly.hideface.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.yesserly.hideface.R;
import com.yesserly.hideface.pojo.Sticker;
import com.yesserly.hideface.views.fragments.EditFragment;

public class StickerRecyclerViewAdapter extends RecyclerView.Adapter<StickerRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "StickerRecyclerViewAdap";

    /***********************************************************************************************
     **************************************** Declarations
     */
    private Sticker[] stickers;
    private EditFragment frag;

    /***********************************************************************************************
     **************************************** Constructor
     */
    public StickerRecyclerViewAdapter(Sticker[] stickers, EditFragment frag) {
        this.stickers = stickers;
        this.frag = frag;
    }

    /***********************************************************************************************
     **************************************** Methods
     */
    @NonNull
    @Override
    public StickerRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sticker_recyclerview_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull StickerRecyclerViewAdapter.ViewHolder holder, final int position) {
        holder.icon.setImageResource(stickers[position].getIcon());
        holder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frag.applySticker(stickers[position]);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (stickers == null) return 0;
        else return stickers.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView icon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
        }
    }
}
