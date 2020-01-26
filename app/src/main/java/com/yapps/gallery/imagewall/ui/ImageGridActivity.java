package com.yapps.gallery.imagewall.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yapps.gallery.R;

/**
 * This activity contains a fragment appears in grid form.
 */
public class ImageGridActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_main);
        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.grid_content, new ImageGridFragmentEx())
                    .commitNow();       //commit is asynchronous method while commitNow is synchronous
        }
    }
}
