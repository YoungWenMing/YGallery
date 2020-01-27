package com.yapps.gallery.imagewall.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import com.yapps.gallery.easyloader.FileUtil;

import java.io.InputStream;


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

    public void loadBitmap(InputStream stream, ImageView view, int height){
        Bitmap bitmap = loadBitmap(stream, height);
        view.setImageBitmap(bitmap);
    }


    public Bitmap loadBitmap(InputStream stream, int height){
        FileUtil fileUtil = new FileUtil();
        byte[] array = null;
        try {
            array = fileUtil.readInputStream(stream);
        }catch (Exception e){
            e.printStackTrace();
        }

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        Bitmap bitmap = null;

        if (array != null){
            BitmapFactory.decodeByteArray(array, 0, array.length, opts);
            int inSampleSize = ImageResizer.calculateInSampleSize(opts, height, height);
            opts.inSampleSize = inSampleSize;
            opts.inJustDecodeBounds = false;

            bitmap = BitmapFactory.decodeByteArray(array, 0, array.length, opts);
        }
        return bitmap;
    }



}
