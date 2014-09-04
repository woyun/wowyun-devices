package com.wowfly.wowyun.wowyun_device;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;

/**
 * Created by user on 9/3/14.
 */
public class VideoBrowserActivity extends MediaBrowserActivity {
    private ArrayList<MediaInfo.VideoInfo> mDataList;
    private static final String TAG = "VideoBrowserActivity";

    private SimpleImageLoadingListener silListener = new SimpleImageLoadingListener() {
        public void onLoadingStarted(String uri, View view) {
        }

        public void onLoadingFailed(String uri, View view, FailReason ret) {
        }

        public void onLoadingComplete(String uri, View view, Bitmap loaded) {

        }
    };

    public void onCreate(Bundle saved) {
        super.onCreate(saved);

        mBrowseType = BROWSE_MEDIA_ALL;
        mType = MEDIA_TYPE_VIDEO;

        mInfo.getVideosInfo(mPref);

        mMediaView.setAdapter(new VideoViewAdapter());

        WowYunApp _app = (WowYunApp) getApplication();
        mHttpClient.get("http://101.69.230.238:8080/list/"+_app.deviceID.toLowerCase(), mHttpHandler);

        displayMediaInfo(R.string.videos_header_status, getVideoCount());
    }


    public class VideoViewAdapter extends BaseAdapter {
        public int getCount() {
            return getVideoCount();
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
                convertView.setLayout(R.layout.grid_video_item);
            }
            imageView = (ImageView) convertView.findViewById(R.id.videothumb);
            Log.i(TAG, "getView pos = " + pos + " imageView = " + imageView);
            //imgLoader.displayImage("video/"+mInfo.getVideoId(pos), imageView, mOption, silListener);
            displayVideo(pos, imageView, mOption, silListener);

            return convertView;
        }
    }
}
