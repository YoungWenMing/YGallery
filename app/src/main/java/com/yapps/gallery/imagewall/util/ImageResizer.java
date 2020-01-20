package com.yapps.gallery.imagewall.util;

import android.graphics.BitmapFactory;
import android.util.Log;

public class ImageResizer {

    /**
     * before the calculation, the bitmap has been checked
     * option's inJustDecodeBounds = true
     * BitmapFactory.decode...(option, ...);
     * so raw height and width can be accessed
     * @param opts
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options opts,
                                            int reqWidth, int reqHeight){
        //raw height and width
        final int h = opts.outHeight, w = opts.outWidth;
        int inSampleSize = 1;
        Log.i("ImageResizer", "required " + reqHeight + ", actual " + h + " " + w);

        if (h > reqHeight || w > reqHeight){
//            final int halfHeight = h, halfWidth = w;
            while ((h/ inSampleSize) > reqHeight
                    && (w / inSampleSize) > reqWidth){
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

}
