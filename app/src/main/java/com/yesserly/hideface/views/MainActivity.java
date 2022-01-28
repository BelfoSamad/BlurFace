package com.yesserly.hideface.views;

import static com.yesserly.hideface.utils.Config.enableAds;
import static com.yesserly.hideface.utils.Config.enableGDPR;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.gms.ads.MobileAds;
import com.yesserly.hideface.R;
import com.yesserly.hideface.utils.GDPR;
import com.yesserly.hideface.viewmodels.MainViewModel;
import com.yesserly.hideface.views.fragments.EditFragment;
import com.yesserly.hideface.views.fragments.HomeFragment;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements HomeFragment.HomeListener, EditFragment.EditListener {
    private static final String TAG = "MainActivity";

    /***********************************************************************************************
     **************************************** Declarations
     */
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_PICK_PHOTO = 2;
    static final String STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    @Inject
    GDPR gdpr;
    private MainViewModel mViewModel;
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, getResources().getString(R.string.permission_msg), Toast.LENGTH_SHORT).show();
                }
            });


    /***********************************************************************************************
     **************************************** LifeCycle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set ViewModel
        mViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        //GDPR Consent
        if (savedInstanceState != null && enableGDPR)
            gdpr.checkForConsent();//Check for user's consent

        //Initialize Ads
        if (enableAds)
            MobileAds.initialize(this, initializationStatus -> {
            });

        //Request Permission
        if (isPermissionUnGranted())
            requestPermission();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "camera");
                    bundle.putString("path", mViewModel.getImage_path());
                    Navigation.findNavController(this, R.id.nav_host_fragment)
                            .navigate(R.id.go_to_edit, bundle);
                }
                break;
            case REQUEST_PICK_PHOTO:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = new Bundle();
                    bundle.putString("type", "gallery");
                    bundle.putString("path", data.getData().toString());
                    Navigation.findNavController(this, R.id.nav_host_fragment)
                            .navigate(R.id.go_to_edit, bundle);
                }
                break;
        }
    }

    /***********************************************************************************************
     **************************************** Methods
     */
    public boolean isPermissionUnGranted() {
        return ContextCompat.checkSelfPermission(this, STORAGE_PERMISSION) != PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission() {
        requestPermissionLauncher.launch(STORAGE_PERMISSION);
    }

    @Override
    public void takePicture() {
        if (isPermissionUnGranted())
            requestPermission();
        else {
            File imageFile = null;
            try {
                imageFile = mViewModel.createImageFile();
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    //Save File
                    Uri photoURI = FileProvider.getUriForFile(this,
                            "com.yesserly.hideface.fileprovider",
                            imageFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    takePictureIntent.putExtra("path", mViewModel.getImage_path());
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                } else
                    Toast.makeText(this, getResources().getString(R.string.camera_not_found), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.d(TAG, "takePicture: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void openGallery() {
        if (isPermissionUnGranted())
            requestPermission();
        else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_image)), REQUEST_PICK_PHOTO);
        }
    }

    @Override
    public void goBack() {
        onBackPressed();
    }
}