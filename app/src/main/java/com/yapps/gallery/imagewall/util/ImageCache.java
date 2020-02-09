package com.yapps.gallery.imagewall.util;

import android.graphics.drawable.BitmapDrawable;
import android.util.LruCache;

public class ImageCache {

    private static final String TAG = "ImageCache";

    private LruCache<String, BitmapDrawable> mImageMemCache;

    private final int LOW_CACHE_SIZE = 1024 * 5,
                    MEDIUM_CACHE_SIZE = 1024 * 10,
                    HIGH_CACHE_SIZE = 1024 * 15; //

    public enum CacheLevel {
            LOW_CACHE, MEDIUM_CACHE, HIGH_CACHE
    }

    public ImageCache(CacheLevel level){
        switch (level){
            case LOW_CACHE:
                mImageMemCache = new LruCache<>(LOW_CACHE_SIZE);
                break;
            case HIGH_CACHE:
                mImageMemCache = new LruCache<>(HIGH_CACHE_SIZE);
                break;
            case MEDIUM_CACHE:
                mImageMemCache = new LruCache<>(MEDIUM_CACHE_SIZE);
                break;
            default:
                break;
        }
    }

    public void addBitmapToCache(String data, BitmapDrawable drawable){
        mImageMemCache.put(data, drawable);
    }

    public BitmapDrawable getBitmapFromCache(String data){
        return mImageMemCache.get(data);
    }

}
