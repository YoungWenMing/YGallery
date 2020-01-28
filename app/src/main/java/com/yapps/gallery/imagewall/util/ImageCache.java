package com.yapps.gallery.imagewall.util;

import android.graphics.drawable.BitmapDrawable;
import android.util.LruCache;

public class ImageCache {

    private LruCache<String, BitmapDrawable> mImageMemCache;

    private final int MEM_CACHE_SIZE = 1024 * 15; //15MB

    public ImageCache(){
        mImageMemCache = new LruCache<>(MEM_CACHE_SIZE);
    }

    public void addBitmapToCache(String data, BitmapDrawable drawable){
        mImageMemCache.put(data, drawable);
    }

    public BitmapDrawable getBitmapFromCache(String data){
        return mImageMemCache.get(data);
    }

}
