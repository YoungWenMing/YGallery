package com.yapps.gallery.easyloader;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.yapps.gallery.R;

public class ImageListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ImageListFragment";

    private ListView listView = null;

    private SimpleCursorAdapter cursorAdapter = null;

    private static final String[] PROJECTION = {MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.LATITUDE,
            MediaStore.Images.Media.LONGITUDE,
            MediaStore.Images.Media._ID         // SimpleCursorAdapter always uses '_id' column
    };

    private static final String ORDER = MediaStore.Images.Media.DISPLAY_NAME + " ASC";

    private Bitmap bitmap = null;
    private byte[] caches = null;

    public static ImageListFragment getInstance(){
        return new ImageListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.image_list_fragment, container, false);

        listView = root.findViewById(R.id.image_list);

        cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item,
                null, PROJECTION, new int[]{R.id.item_title, R.id.item_detail, }, 0);
        cursorAdapter.setViewBinder(new ImageLocationBinder());
        listView.setAdapter(cursorAdapter);
        listView.setOnItemClickListener(new ImageInfoClickListener());
        LoaderManager.getInstance(this).restartLoader(0, null, this);

        return root;
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

        CursorLoader loader = new CursorLoader(getActivity(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                PROJECTION,
                null,
                null,
                ORDER);
        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data);
        if (data != null){
            data.moveToFirst();
            String first = data.getString(data.getColumnIndex( MediaStore.Images.ImageColumns.DISPLAY_NAME));
            Log.i(TAG, " first image named " + first);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
        Log.i(TAG, "Loader Reset");
    }

    private class ImageLocationBinder implements SimpleCursorAdapter.ViewBinder{

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (columnIndex == 1){
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
                ((TextView) view).setText("ID- " + id);
                return true;
            }
            return false;
        }
    }

    private class ImageInfoClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Dialog dialog = new Dialog(getActivity());
            dialog.setContentView(R.layout.dialog_main);
            dialog.setTitle("图片显示");
            dialog.setCancelable(true);

            ImageView image = dialog.findViewById(R.id.image_show);

            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
//                    recycle the memory if dialog is canceled.
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                }
            });

//          build uri of external images and then append path to the end
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
            Log.i(TAG, "we get uri of this image " + uri.toString());
            FileUtil file = new FileUtil();

            ContentResolver resolver = getActivity().getContentResolver();
            try{
                /**
                 * content resolver help us get all bytes of this image
                 * and put them into the caches
                 */
                caches = file.readInputStream(resolver.openInputStream(uri));
                bitmap = file.getBitmapFromBytes(caches, null);
                image.setImageBitmap(bitmap);
            }catch (Exception e){
                e.printStackTrace();
            }
            dialog.show();

        }
    }

}
