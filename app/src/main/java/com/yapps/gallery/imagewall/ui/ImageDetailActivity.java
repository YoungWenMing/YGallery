package com.yapps.gallery.imagewall.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.Toolbar;

import com.yapps.gallery.R;
import com.yapps.gallery.imagewall.util.CursorBitmapFetcher;
import com.yapps.gallery.imagewall.util.ImageCache;

public class ImageDetailActivity extends AppCompatActivity implements View.OnClickListener,
        GestureDetector.OnDoubleTapListener {

    private static String TAG = "ImageAct";

    public static final String INIT_IMAGE = "initial_image";

    private CursorBitmapFetcher mFetcher;

    private ViewPager mPager;

    private ImagePageAdapter mAdapter ;

    private int numPages;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_detail_main);

        //TODO:find pointer of views
        mPager = findViewById(R.id.pager);
        final Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);   //make the toolbar act as an action bar

        //TODO:adjust the view to screen,
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels, width = displayMetrics.widthPixels;
        int longest = (height > width ) ? height : width;

        //TODO:initialize the imageFetcher
        mFetcher = new CursorBitmapFetcher(this, longest);
        mFetcher.addImageCache(ImageCache.CacheLevel.HIGH_CACHE);

        //TODO:set the view pager
        numPages = mFetcher.getCount();
        mAdapter = new ImagePageAdapter(getSupportFragmentManager(), numPages);
        mPager.setAdapter(mAdapter);
        mPager.setPageMargin((int) getResources().getDimension(R.dimen.horizontal_page_margin));
        mPager.setOffscreenPageLimit(0);

        //set the action bar
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayShowTitleEnabled(false);    //hide the title
            actionBar.setDisplayHomeAsUpEnabled(true);      //display back bottom
            mPager.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0){
                        actionBar.hide();
                    }else {
                        actionBar.show();
                    }
                }
            });

            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            actionBar.hide();
        }

        //the index of first item to show
        int curItemNum = getIntent().getIntExtra(INIT_IMAGE, -1);
        if (curItemNum != -1){
            mPager.setCurrentItem(curItemNum);
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ImagePageAdapter extends FragmentStatePagerAdapter {
        private int mSize ;

        ImagePageAdapter(FragmentManager fm, int count){
            super(fm);
            mSize = count;
        }

        @Override
        public Fragment getItem(int position) {
            return ImageDetailFragment.getInstance(position);
        }

        @Override
        public int getCount() {
            return mSize;
        }
    }

    public CursorBitmapFetcher getFetcher(){
        return mFetcher;
    }

    @Override
    public void onClick(View v) {
        final int vis = mPager.getSystemUiVisibility();
        if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0 ){
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }else {
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Log.i(TAG, "doubleTap occurs!");
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }
}
