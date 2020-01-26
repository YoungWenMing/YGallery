package com.yapps.gallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;
import com.yapps.gallery.easyloader.LoaderActivity;
import com.yapps.gallery.imagewall.ui.ImageGridActivity;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MainActivity extends Activity implements View.OnClickListener {

    private final String TAG = "MainAct";
    private final int EXTERNAL_STORAGE_REQUEST_CODE = 0x0;

    Button btnLoader, btnWall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnLoader = this.findViewById(R.id.loader_button);
        btnLoader.setOnClickListener(this);
        btnWall = this.findViewById(R.id.wall_button);
        btnWall.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.loader_button:
                Intent intent = new Intent(this, LoaderActivity.class);
                startActivity(intent);
                break;
            case R.id.wall_button:
                if (ActivityCompat.checkSelfPermission(
                        this, Manifest.permission.READ_EXTERNAL_STORAGE )!= PackageManager.PERMISSION_GRANTED){
                    requestStoragePermission();
                }else {
                    Intent intent1 = new Intent(this, ImageGridActivity.class);
                    startActivity(intent1);
                }
                break;
            default:
                break;
        }
    }

    public void requestStoragePermission() {
        Log.i(TAG, "Request external storage permission directly!");
        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_REQUEST_CODE);
    }
}
