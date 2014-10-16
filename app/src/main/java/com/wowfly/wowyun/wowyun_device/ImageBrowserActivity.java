package com.wowfly.wowyun.wowyun_device;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;

/**
 * Created by user on 9/3/14.
 */
public class ImageBrowserActivity extends MediaBrowserActivity {
    private static final String TAG = "ImageBrowserActivity";

    private SimpleImageLoadingListener silListener = new SimpleImageLoadingListener() {
        public void onLoadingStarted(String uri, View view) {
        }

        public void onLoadingFailed(String uri, View view, FailReason ret) {
        }

        public void onLoadingComplete(String uri, View view, Bitmap loaded) {

        }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                showBrowseTypeSelectionDialog(this, R.string.imagebrowsetype, R.array.imagebrowsetype);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onCreate(Bundle saved) {
        int count = 0;
        super.onCreate(saved);

        mBrowseType = mPref.getInt("browse.image.type", WowYunApp.BROWSE_MEDIA_ALL);
        //mBrowseType = BROWSE_MEDIA_ALL;
        mType = MEDIA_TYPE_IMAGE;

        mInfo.getImagesInfo(mPref);

        mMediaView.setAdapter(new ImageViewAdapter());
        mMediaView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
/*                if(mBrowseType == WowYunApp.BROWSE_MEDIA_ALL) {
                    if(i >= mInfo.getImageCount()) {
                        i = -(i - mInfo.getImageCount());
                    }
                } else if(mBrowseType == WowYunApp.BROWSE_MEDIA_WEBALBUM) {
                    i = -i;
                }*/
                Intent intent = new Intent(mContext, ImageViewer.class);
                intent.putExtra("position", i);
                intent.putExtra("browsetype", mBrowseType);
                startActivity(intent);
            }
        });

        WowYunApp _app = (WowYunApp) getApplication();
        mHttpClient.get("http://101.69.230.238:8080/list/"+_app.deviceID.toLowerCase(), mHttpHandler);
        Log.i(TAG, " url = " + "http://101.69.230.238:8080/list/" + _app.deviceID);

        displayMediaInfo(R.string.images_header_status, getImageCount());
    }

    public void onDestroy() {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putInt("browse.image.type", mBrowseType);
        editor.commit();

        super.onDestroy();
    }

    public class ImageViewAdapter extends BaseAdapter {
        public int getCount() {
            return getImageCount();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int pos, View view, ViewGroup parent) {
            GridItem convertView;
            ImageView imageView;

            convertView = (GridItem) view;
            if(convertView == null) {
                convertView = new GridItem(getBaseContext());
                convertView.setLayout(R.layout.grid_image_item);
            }

            imageView = (ImageView)convertView.findViewById(R.id.imagethumb);
            Log.i(TAG, "getView pos = " + pos + " imageView = " + imageView);
            displayImage(pos, imageView, mOption, silListener);
            //imgLoader.displayImage("image/"+mInfo.getImageId(pos), imageView, mOption, silListener);

            return convertView;
        }
    }
}
