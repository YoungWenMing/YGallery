package com.yapps.gallery.imagewall.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.yapps.gallery.R;
import com.yapps.gallery.imagewall.util.BitmapFetcher;
import com.yapps.gallery.imagewall.util.CursorBitmapFetcher;
import com.yapps.gallery.imagewall.view.DetailImageView;

public class ImageDetailFragment extends Fragment implements BitmapFetcher.OnImageLoadedListener {

    public static final String IMAGE_NUM = "image_num";

    private static final String TAG = "DetailFrag";
    private int itemNum;

    private DetailImageView imageView;

    private ProgressBar progressBar;

    public static ImageDetailFragment getInstance(int position){
        final ImageDetailFragment f = new ImageDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(IMAGE_NUM, position);
        f.setArguments(bundle);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        itemNum = getArguments().getInt(IMAGE_NUM);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.image_detail_fragment, container, false);
        imageView = root.findViewById(R.id.image_detail_view);
        progressBar = root.findViewById(R.id.progress_bar);
        ((ViewGroup) imageView.getParent()).removeView(imageView);
        return imageView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        if (activity instanceof ImageDetailActivity){
            CursorBitmapFetcher fetcher = ((ImageDetailActivity) getActivity()).getFetcher();
            fetcher.loadBitmap(itemNum, imageView , this);
        }

        if (activity instanceof View.OnClickListener){
            imageView.setOnClickListener((View.OnClickListener) activity);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onImageLoaded(boolean success) {
        progressBar.setVisibility(View.GONE);
    }
}
