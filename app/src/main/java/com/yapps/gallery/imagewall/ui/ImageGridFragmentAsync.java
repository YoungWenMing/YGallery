package com.yapps.gallery.imagewall.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yapps.gallery.R;
import com.yapps.gallery.imagewall.util.BitmapFetcher;
import com.yapps.gallery.imagewall.util.BitmapLoader;
import com.yapps.gallery.imagewall.util.ImageCache;

import java.io.IOException;
import java.io.InputStream;

public class ImageGridFragmentAsync extends Fragment implements AdapterView.OnItemClickListener {

    private static final String TAG = "GridFragmentAsync";

    private static final String[] PROJECTION = {
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media._ID
    };

    private static final String ORDER = MediaStore.Images.Media.DISPLAY_NAME + " ASC";
    private int mImageThumbSize ;
    private int mImageThumbSpacing ;

    private Cursor cursor;

    private ImageAdapter mAdapter = null;

    private ContentResolver resolver = null;

    private BitmapFetcher mFetcher;

    public ImageGridFragmentAsync(){    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, " inside onCreate");

        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

        cursor = getActivity().getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, PROJECTION, null, null, ORDER);
        Log.i(TAG, "cursor nums " + cursor.getCount());
        mAdapter = new ImageAdapter(getActivity());

        resolver = getActivity().getContentResolver();
        mFetcher = new BitmapFetcher(getActivity(), mImageThumbSize);
        mFetcher.setLoadingBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.empty_photo));
        mFetcher.addImageCache(ImageCache.CacheLevel.MEDIUM_CACHE);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_grid_image, container, false);
        final GridView gridView = (GridView) view.findViewById(R.id.grid_view);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(this);

        gridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mAdapter.getNumColumns() == 0){

                    final int numColumns = (int) Math.floor(
                            gridView.getWidth() / (mImageThumbSpacing + mImageThumbSize)
                    );
                    Log.i(TAG, "calculating column number with width = " + gridView.getWidth() + " numColumns = " + numColumns);
                    if (numColumns > 0){
                        final int columnWidth = (gridView.getWidth() / numColumns) - mImageThumbSpacing;
                        mAdapter.setItemHeight(columnWidth);
                        mAdapter.setNumColumns(numColumns);
                        gridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            }
        });

        return gridView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cursor.close();
    }


    private class ImageAdapter extends BaseAdapter{

        private final Context mContext;
        private int itemHeight = 0;
        private int numColumns = 0;
        private GridView.LayoutParams mImageViewLayoutParams;
        private BitmapLoader mBitmapLoader;

        ImageAdapter(Context context){
            super();
            mContext = context;
            mImageViewLayoutParams = new GridView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            );
            mBitmapLoader = new BitmapLoader(context);
        }

        public int getCount(){
            return cursor.getCount();
        }

        public Object getItem(int position){
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.i(TAG, "inflating the image indexed " + position);
            ImageView imageView ;
            if (convertView == null){
//                Log.i(TAG, " convertView is null");
                imageView = new ImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setLayoutParams(mImageViewLayoutParams);
            }else {
                imageView = (ImageView) convertView;
            }

            if (imageView.getLayoutParams().height != itemHeight){
                imageView.setLayoutParams(mImageViewLayoutParams);
            }

            if (convertView != null && imageView.getLayoutParams().height != 0){
                Uri uri = buildUri(position);
//                Log.i(TAG, " uri is " + uri.toString());
                try {
                    mFetcher.loadBitmap(uri, imageView);
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            return imageView;
        }

        public int getNumColumns() {
            return numColumns;
        }

        public void setNumColumns(int n){
            numColumns = n;
        }

        public void setItemHeight(int height){
            if (height == itemHeight){
                return;
            }
            itemHeight = height;
            mImageViewLayoutParams = new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight);
            //it always inflate all views again if the data set changed.
            notifyDataSetChanged();
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), "Item clicked with position= " + position + ", id= " + id, Toast.LENGTH_SHORT).show();
    }

    private Uri buildUri(int position){
        cursor.moveToPosition(position);
        Long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID));
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
        return uri;
    }
}
