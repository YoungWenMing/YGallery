package com.yapps.gallery.imagewall.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.IOException;

public class BitmapFetcher {

    private Bitmap mLoadingBitmap = null;

    private Context mContext = null;

    private int mHeight, mWidth;

    private final BitmapLoader mBitmapLoader;

    private final ContentResolver mResolver;

    public BitmapFetcher(Context context, int size){
        this(context, size, size);
    }

    public BitmapFetcher(Context context, int height, int width){
        mContext = context;
        mHeight = height;
        mWidth = width;
        mBitmapLoader = new BitmapLoader(context);
        mResolver = context.getContentResolver();
    }

    public void setLoadingBitmap(Bitmap loadingBitmap){
        mLoadingBitmap = loadingBitmap;
    }

    public void loadBitmap(Uri uri, ImageView imageView) throws IOException {
        //TODO:apply asynctask here to load a bitmap

    }

    private class BitmapLoadTask extends AsyncTask<Void, Void, BitmapDrawable>{


        @Override
        protected BitmapDrawable doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(BitmapDrawable bitmapDrawable) {
            super.onPostExecute(bitmapDrawable);
        }
    }

}
