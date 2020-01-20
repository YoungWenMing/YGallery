package com.yapps.gallery;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.yapps.gallery.easyloader.LoaderActivity;
import com.yapps.gallery.imagewall.ui.ImageGridActivity;

public class MainActivity extends Activity implements View.OnClickListener {


    Button btnLoader, btnWall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnLoader = (Button) this.findViewById(R.id.loader_button);
        btnLoader.setOnClickListener(this);
        btnWall = (Button) this.findViewById(R.id.wall_button);
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
                Intent intent1 = new Intent(this, ImageGridActivity.class);
                startActivity(intent1);
                break;
            default:
                break;
        }
    }
}
