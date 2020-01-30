package com.yapps.gallery.imagewall.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.io.IOException;

public class CursorBitmapFetcher extends BitmapFetcher {

    private Cursor cursor;

    private static final String[] PROJECTION = {
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media._ID
    };

    private static final String ORDER = MediaStore.Images.Media.DISPLAY_NAME + " ASC";

    public CursorBitmapFetcher(Context context, int height, int width){
        super(context, height, width);
        cursor = mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, PROJECTION, null, null, ORDER);
    }

    public CursorBitmapFetcher(Context context, int size){
        this(context, size, size);
    }

    /**
     * load a bitmap just according to the position
     * @param position
     * @param imageView
     */
    public void loadBitmap(int position, ImageView imageView){
        Uri uri = buildUri(position);
        if (uri != null){
            try {
                super.loadBitmap(uri, imageView);
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private Uri buildUri(int position){
        Uri uri = null;
        if (cursor.moveToPosition(position)){
            Long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID));
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        }
        return uri;
    }


    public int getCount(){
        return cursor.getCount();
    }

}
