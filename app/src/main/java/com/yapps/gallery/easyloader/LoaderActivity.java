package com.yapps.gallery.easyloader;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.yapps.gallery.R;


/***
 * loader 是用来加载特定数据的类，activity或者fragment通过
 * LoaderManager的回调方法来操作loader
 */

public class LoaderActivity extends AppCompatActivity {

    private static final String TAG = "LoaderActivity";

    private final int EXTERNAL_STORAGE_REQUEST_CODE = 0x0;

    private View mLayout = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loader_main);
        mLayout =  findViewById(R.id.sample_main_layout);

//        get the ListView object
        // inflate the fragment
        if (savedInstanceState == null){

            RequestPermissionFragment fragment = new RequestPermissionFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }

    }

    public void showImagesInfo(View v){
//        check permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE )!= PackageManager.PERMISSION_GRANTED){
            requestStoragePermission();
        }else {
            loadImages();
        }
    }


    /**
     * //  try to get permission to access external storage
     * if same request was denied previously, a SnackBar will prompt the user to grant the permission,
     * it is requested directly otherwise.
     */
    public void requestStoragePermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
            Log.i(TAG, "Displaying rationale snackbar for external storage access");
            Snackbar.make(mLayout, "This app is born with desire to storage permission, please!",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_REQUEST_CODE);
                        }
                    }).show();
        }else {
            Log.i(TAG, "Request external storage permission directly!");
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_REQUEST_CODE);
        }

    }

    /**
     * load all images from external storage
     */
    public void loadImages(){
        Log.i(TAG, "do loadImages by replacing the frame with a fragment.");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.sample_content_fragment, ImageListFragment.getInstance())
                .addToBackStack("images")               //add this fragment to stack
                .commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case EXTERNAL_STORAGE_REQUEST_CODE:
                Toast.makeText(this, "Access to external storage permitted!", Toast.LENGTH_SHORT).show();
                loadImages();
                return;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        getSupportFragmentManager().popBackStack();
    }
}
