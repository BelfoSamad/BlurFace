package com.yesserly.hideface.views.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yesserly.hideface.databinding.HomeFragmentBinding;
import com.yesserly.hideface.ui.adapters.SlideShowAdapter;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    /***********************************************************************************************
     **************************************** Listener
     */
    public interface HomeListener {

        void takePicture();

        void openGallery();

    }

    /***********************************************************************************************
     **************************************** Declarations
     */
    private HomeFragmentBinding mBinding;
    private HomeListener listener;

    /***********************************************************************************************
     **************************************** LifeCycle
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        listener = (HomeListener) context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        //Set mBinding
        mBinding = HomeFragmentBinding.inflate(inflater, container, false);

        //Init SlideShow
        initSlideshow();

        //When Camera Button is clicked
        mBinding.camera.setOnClickListener(v -> listener.takePicture());

        //When Gallery Button is clicked
        mBinding.gallery.setOnClickListener(v -> listener.openGallery());

        return mBinding.getRoot();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }

    /***********************************************************************************************
     **************************************** Methods
     */
    private void initSlideshow() {
        SlideShowAdapter mAdapter = new SlideShowAdapter(getContext());
        mBinding.slide.setAdapter(mAdapter);
    }
}