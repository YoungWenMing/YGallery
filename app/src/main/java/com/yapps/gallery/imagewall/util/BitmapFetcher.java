package com.yapps.gallery.imagewall.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BitmapFetcher {

    private static final String TAG = "Fetcher";

    private Bitmap mLoadingBitmap = null;

    Context mContext ;

    private int mHeight, mWidth;

    private final BitmapLoader mBitmapLoader;

    private final ContentResolver mResolver;

    private ImageCache mImageCache;

    private ExecutorService mPool = null;

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

    public void addImageCache(ImageCache.CacheLevel level){
        if (mImageCache == null){
            mImageCache = new ImageCache(level);
        }
    }

    public void addThreadPool(){
        mPool =  Executors.newFixedThreadPool(2);
    }

    public void setLoadingBitmap(Bitmap loadingBitmap){
        mLoadingBitmap = loadingBitmap;
    }

    /**
     * load a target bitmap by an uri into the imageView with an asyncTask
     * when the asyncTask is running, a bitmap indicates loading the final
     * bitmap will occupy the imageView temporarily.
     * @param uri           the final image's uri
     * @param imageView
     * @throws IOException
     */
    public void loadBitmap(Uri uri, ImageView imageView, OnImageLoadedListener listener) throws IOException {
        //TODO:apply asynctask here to load a bitmap, cache will be considered
        //
//        Log.i(TAG, "loading bitmap for " + imageView.getId());
        BitmapDrawable value = null;
        if (mImageCache != null){
            value = mImageCache.getBitmapFromCache(uri.toString());
        }

        //firstly consider that whether the drawable is cached in memory
        if (value != null){
            imageView.setImageDrawable(value);
            if (listener != null){
                listener.onImageLoaded(true);
            }
        }else {
            imageView.setImageBitmap(mLoadingBitmap);
            BitmapLoadTask task = new BitmapLoadTask(uri, imageView, listener);
            if (mPool == null){
                task.execute();
            }else {
                task.executeOnExecutor(mPool);
            }

        }
    }

    public void loadBitmap(Uri uri, ImageView imageView) throws IOException{
        loadBitmap(uri, imageView, null);
    }

    /**
     * listener on image loading
     */
    public static interface OnImageLoadedListener{

        public void onImageLoaded(boolean success);
    }

    private class BitmapLoadTask extends AsyncTask<Void, Void, BitmapDrawable>{

        private WeakReference<ImageView> imageViewWeakReference;
        private Uri mUri;
        private OnImageLoadedListener mListener;

        public BitmapLoadTask(Uri uri, ImageView view){
            imageViewWeakReference = new WeakReference<ImageView>(view);
//            mImageView = view;
            mUri = uri;
        }

        public BitmapLoadTask(Uri uri, ImageView view, OnImageLoadedListener listener){
            this(uri, view);
            mListener = listener;
        }

        @Override
        protected BitmapDrawable doInBackground(Void... voids) {
//            Log.i(TAG, Thread.currentThread() + " doing task in back ground");
            BitmapDrawable bitmap = null;
            try {
                InputStream stream = mResolver.openInputStream(mUri);
                bitmap = new BitmapDrawable( mContext.getResources(), mBitmapLoader.loadBitmap(stream, mHeight));
            }catch (IOException e){
                e.printStackTrace();
            }
            //when a bitmap is loaded for first time, add it to the cache
            if (bitmap != null){
                if (mListener != null)
                    mListener.onImageLoaded(true);
                if (mImageCache != null){
                    mImageCache.addBitmapToCache(mUri.toString(), bitmap);
                }
            }else if (mListener != null){
                mListener.onImageLoaded(false);
            }
            return bitmap;
        }

        @Override
        protected void onCancelled() {
            Log.i(TAG, "AsyncTask is cancelled.");
            super.onCancelled();
        }

        /**
         * @param bitmapDrawable
         */
        @Override
        protected void onPostExecute(BitmapDrawable bitmapDrawable) {
            ImageView view = imageViewWeakReference.get();
            setImageDrawable(view, bitmapDrawable);
            super.onPostExecute(bitmapDrawable);
        }
    }

    /**
     * set the view's bitmap to the specified bitmap
     * @param view
     * @param value
     */
    private void setImageDrawable(ImageView view, Drawable value){
        if (view != null){
//            final TransitionDrawable td =
//                    new TransitionDrawable(new Drawable[] {
//                            new ColorDrawable(Color.TRANSPARENT),
//                            value
//                    });
            // Set background to loading bitmap
//            view.setBackground(
//                    new BitmapDrawable(mContext.getResources(), mLoadingBitmap));
//
//            view.setImageDrawable(td);
//            td.startTransition(200);
//            view.setImageDrawable(value);
            view.setImageDrawable(value);
        }
    }

}
