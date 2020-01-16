package com.yapps.gallery;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.yapps.gallery.easyloader.LoaderActivity;

public class MainActivity extends Activity implements View.OnClickListener {


    Button btnLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnLoader = (Button) this.findViewById(R.id.loader_button);
        btnLoader.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.loader_button:
                Intent intent = new Intent(this, LoaderActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
