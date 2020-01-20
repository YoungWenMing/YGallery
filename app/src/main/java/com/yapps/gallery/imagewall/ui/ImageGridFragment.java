package com.yapps.gallery.imagewall.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
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
import com.yapps.gallery.imagewall.util.BitmapLoader;

public class ImageGridFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String TAG = "ImageGridFragment";

    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private int mItemHeight;

    //need an adapter
    private ImageAdapter mAdapter;
    //image id set
    private static final int[] IMAGE_IDs = {R.drawable.a1, R.drawable.a2,R.drawable.a3,R.drawable.a4,R.drawable.a5,};

    public ImageGridFragment(){ }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "inside onCreate");

        //initialize some layout params
        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

        mAdapter = new ImageAdapter(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_grid_image, container, false);
        final GridView gridView = view.findViewById(R.id.grid_view);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(this);

        //添加布局过程监听，获取当前屏幕的宽高，进而计算每个网格的宽高
        gridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mAdapter.getmNumColumns() == 0){
                    Log.i(TAG, "calculating column number with width = " + gridView.getWidth());
                    // 向下取整获得列数量
                    final int numColumns = (int) Math.floor(
                            gridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
                    if (numColumns > 0){
                        final int columnWidth = (gridView.getWidth() / numColumns) - mImageThumbSpacing;
                        mAdapter.setmItemHeight(columnWidth);
                        mAdapter.setmNumColumns(numColumns);
                        // 这个设置只会完成一次，一次之后不会再监听
                        gridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            }
        });

        return view;
    }


    private class ImageAdapter extends BaseAdapter{

        // context object is needed to get resources
        private final Context mContext;

        private int mItemHeight = 0;
        private int mNumColumns = 0;
        private GridView.LayoutParams mImageViewLayoutParams;

        private BitmapLoader mBitmaploader;

        ImageAdapter(Context context){
            super();
            mContext = context;
            // decide the layout pattern of an imageview in grid
            mImageViewLayoutParams = new GridView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            );
            mBitmaploader = new BitmapLoader(context);
        }

        @Override
        public int getCount() {
            return 5;   //5 images are going to be show in the grid
        }

        @Override
        public Object getItem(int position) {
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
            ImageView imageView;
            if (convertView == null){
                imageView = new ImageView(mContext);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setLayoutParams(mImageViewLayoutParams);
            }else {
                imageView = (ImageView) convertView;
            }

            //adjust the item params to fit the grid
            if (imageView.getLayoutParams().height != mItemHeight){
                imageView.setLayoutParams(mImageViewLayoutParams);
            }
            if (convertView == null){
                mBitmaploader.loadBitmap(IMAGE_IDs[position], imageView, mItemHeight);
            }
            return imageView;
        }

        public int getmNumColumns() {
            return mNumColumns;
        }

        public void setmNumColumns(int n){
            mNumColumns = n;
        }

        public void setmItemHeight(int height){
            if (height == mItemHeight){
                return;
            }
            mItemHeight = height;
            mImageViewLayoutParams = new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mItemHeight);
            notifyDataSetChanged();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), "Item clicked with position= " + position + ", id= " + id, Toast.LENGTH_SHORT).show();
    }
}
