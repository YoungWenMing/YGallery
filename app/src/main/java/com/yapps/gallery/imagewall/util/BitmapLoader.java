package com.yapps.gallery.imagewall.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

public class BitmapLoader {

    private Context mContext;

//    private ImageView mImageView;

    public BitmapLoader(Context context){
        this.mContext = context;
//        this.mImageView = imageView;
    }

    /**
     * load image as a bitmap with a resource id into the image view
     * if the bitmap is too large, it will be sampled according to desired height.
     * @param resId
     * @param view
     * @param height
     */
    public void loadBitmap(int resId, ImageView view, int height){
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(mContext.getResources(), resId, opts);

        int inSampleSize = ImageResizer.calculateInSampleSize(opts, height, height);
        opts.inSampleSize = inSampleSize;
        opts.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), resId, opts);
        view.setImageBitmap(bitmap);
    }



}
