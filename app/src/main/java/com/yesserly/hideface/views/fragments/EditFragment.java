package com.yesserly.hideface.views.fragments;

import static com.yesserly.hideface.utils.Config.enableAds;
import static com.yesserly.hideface.utils.Config.enableGDPR;
import static com.yesserly.hideface.utils.Config.stickers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.yesserly.hideface.R;
import com.yesserly.hideface.databinding.EditFragmentBinding;
import com.yesserly.hideface.pojo.Area;
import com.yesserly.hideface.pojo.Sticker;
import com.yesserly.hideface.ui.adapters.StickerRecyclerViewAdapter;
import com.yesserly.hideface.utils.BitmapHandler;
import com.yesserly.hideface.utils.ClickableAreasImage;
import com.yesserly.hideface.utils.GDPR;
import com.yesserly.hideface.viewmodels.EditViewModel;
import com.yesserly.hideface.views.MainActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.mlkit.vision.face.Face;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import htk.lib.areasimage.ClickableArea;
import htk.lib.areasimage.OnClickableAreaClickedListener;

@AndroidEntryPoint
public class EditFragment extends Fragment implements OnClickableAreaClickedListener {
    private static final String TAG = "EditFragment";

    /***********************************************************************************************
     **************************************** Listener
     */
    public interface EditListener {
        void goBack();
    }

    /***********************************************************************************************
     **************************************** Declarations
     */
    private EditViewModel mViewModel;
    private StickerRecyclerViewAdapter mAdapter;
    private EditFragmentBinding mBinding;
    private EditListener listener;
    private String type;
    private String path;
    private BitmapHandler bitmapHandler;

    @Inject
    GDPR gdpr;
    private InterstitialAd mInterstitialAd;

    final OnClickableAreaClickedListener thisListener = this;
    final Observer<List<Face>> facesObserver = faces -> {
        if (faces == null)
            Toast.makeText(getContext(), getResources().getString(R.string.detect_faces_error), Toast.LENGTH_SHORT).show();
        else {
            if (faces.isEmpty()) {
                Snackbar bar = Snackbar.make(mBinding.getRoot(), getResources().getString(R.string.detect_faces_empty), Snackbar.LENGTH_LONG);
                bar.getView().setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.errorColor));
                bar.setActionTextColor(Color.WHITE);
                bar.show();
            } else {
                //Create Clickable Areas
                ClickableAreasImage clickableAreasImage = new ClickableAreasImage(mBinding.image, thisListener);
                // set enable zoom by two finger or double tap
                clickableAreasImage.setEnableScalePicture(false);

                //Get Rects
                List<ClickableArea> clickableAreas = new ArrayList<>();
                ArrayList<Rect> rects = new ArrayList<>();
                for (Face face :
                        faces) {
                    Rect rect = face.getBoundingBox();
                    rects.add(rect);
                    clickableAreas.add(new ClickableArea(rect.left, rect.top, rect.width(), rect.height(),
                            new Area(rect, false, false)));
                }

                //Create Bitmap
                bitmapHandler.createEmptyBitmap(bitmapHandler.getSource().getWidth(), bitmapHandler.getSource().getHeight());
                mBinding.areas.setImageBitmap(bitmapHandler.addAreas(rects));//Add Rects and Set Image

                //Set Clickable ImageAreas
                clickableAreasImage.setClickableAreas(clickableAreas);
            }
        }

        //Hide Progress Bar
        mBinding.progress.setVisibility(View.GONE);
    };

    /***********************************************************************************************
     **************************************** LifeCycle
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (MainActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            path = getArguments().getString("path");
            type = getArguments().getString("type");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //Init Binding
        mBinding = EditFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Setup ViewModel
        mViewModel = new ViewModelProvider(this).get(EditViewModel.class);

        //Init Interstitial Ad
        if (enableGDPR)
            gdpr.loadInterstitialAd(requireContext(), new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    super.onAdLoaded(interstitialAd);
                    mInterstitialAd = interstitialAd;
                }
            });
        else {
            AdRequest adRequest = new AdRequest.Builder().build();
            InterstitialAd.load(requireContext(), getResources().getString(R.string.INTERSTITIAL_AD_ID), adRequest, new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    super.onAdLoaded(interstitialAd);
                    mInterstitialAd = interstitialAd;
                }
            });
        }

        //Init StickersRecyclerView
        initStickerRecyclerView();

        //Prepare Image
        bitmapHandler = new BitmapHandler(getContext());
        if (type.equals("camera")) {
            mBinding.image.setImageBitmap(bitmapHandler.createBitmap(type, path,
                    mViewModel.getCameraPhotoOrientation(path)));
        } else {
            mBinding.image.setImageBitmap(bitmapHandler.createBitmap(type, path,
                    mViewModel.getCameraPhotoOrientation(getContext(), Uri.parse(path))));
        }

        //Scan Image
        mViewModel.DetectFaces(bitmapHandler.getSource(), 0);

        //Start ProgressBar
        mBinding.progress.setVisibility(View.VISIBLE);

        //Set Observer For Faces
        mViewModel.getDetectedFaces().observe(getViewLifecycleOwner(), facesObserver);

        //Listeners
        mBinding.back.setOnClickListener(v -> this.listener.goBack());
        mBinding.clear.setOnClickListener(v -> clearStickers());
        mBinding.save.setOnClickListener(v -> saveImage());
        mBinding.share.setOnClickListener(v -> shareImage());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewModel.getDetectedFaces().removeObserver(facesObserver);
    }

    /***********************************************************************************************
     **************************************** Methods
     */

    private void initStickerRecyclerView() {
        mAdapter = new StickerRecyclerViewAdapter(stickers, this);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
        mBinding.stickerRecyclerview.setLayoutManager(manager);
        mBinding.stickerRecyclerview.setAdapter(mAdapter);
    }

    public void applySticker(Sticker sticker) {
        //Apply New Sticker
        Bitmap dest = BitmapFactory.decodeResource(getResources(), sticker.getIcon());
        Bitmap bit = bitmapHandler.applyStickers(dest, mViewModel.getSelectedAreas());

        if (bit == null) {
            Snackbar bar = Snackbar.make(mBinding.getRoot(), getResources().getString(R.string.no_face_selected), Snackbar.LENGTH_LONG);
            bar.getView().setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.errorColor));
            bar.setActionTextColor(Color.WHITE);
            bar.show();
        } else mBinding.image.setImageBitmap(bit);


        mViewModel.setEdited(true);
    }

    private void clearStickers() {
        mBinding.image.setImageBitmap(bitmapHandler.clearStickers());
        mViewModel.setEdited(false);
    }

    private void saveImage() {
        if (mViewModel.isEdited()) {
            bitmapHandler.saveBitmap();

            //Set Snackbar (Message)
            if (!enableAds || mInterstitialAd != null) {
                mInterstitialAd.show(requireActivity());
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setType("image/*");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                });
            } else
                Snackbar.make(mBinding.getRoot(), getResources().getString(R.string.image_saved), Snackbar.LENGTH_LONG)
                        .setAction(getResources().getString(R.string.go_gallery), v1 -> {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setType("image/*");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        })
                        .show();
        } else {
            Snackbar bar = Snackbar.make(mBinding.getRoot(), getResources().getString(R.string.image_not_edited), Snackbar.LENGTH_LONG);
            bar.getView().setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.errorColor));
            bar.setActionTextColor(Color.WHITE);
            bar.show();
        }
    }

    private void shareImage() {
        if (mViewModel.isEdited()) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, bitmapHandler.getFileUri());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/png");
            startActivity(intent);
        } else {
            Snackbar bar = Snackbar.make(mBinding.getRoot(), getResources().getString(R.string.image_not_edited), Snackbar.LENGTH_LONG);
            bar.getView().setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.errorColor));
            bar.setActionTextColor(Color.WHITE);
            bar.show();
        }
    }

    @Override
    public void onClickableAreaTouched(ClickableArea.State state, float v, float v1, float v2, float v3) {
        Log.d(TAG, "onClickableAreaTouched: Image Clicked");

        //Get Blurred Area
        Area ar = (Area) state;

        if (!ar.isSelected()) {
            ar.setSelected(true);
            mViewModel.addToSelected(ar);
            mBinding.areas.setImageBitmap(bitmapHandler.selectArea(ar.getRect()));
        } else {
            ar.setSelected(false);
            mViewModel.removeFromSelected(ar);
            mBinding.areas.setImageBitmap(bitmapHandler.unSelectArea(ar.getRect()));
        }
    }

}