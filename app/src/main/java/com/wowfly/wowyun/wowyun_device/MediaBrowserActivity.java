package com.wowfly.wowyun.wowyun_device;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecoder;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.apache.http.Header;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by user on 9/3/14.
 */
public class MediaBrowserActivity extends Activity {
    private static final String TAG = "MediaBrowserActivity";

    protected MediaInfo mInfo;
    private ImageLoaderConfiguration mConfig;
    protected Context mContext;
    protected DisplayImageOptions mOption;
    protected ImageLoader imgLoader = ImageLoader.getInstance();
    protected SharedPreferences mPref;
    protected TextView mInfoHeader;
    protected GridView mMediaView;
    protected AsyncHttpClient mHttpClient;
    protected ArrayList<WebAlbumInfo> mWebAlbumImageList;
    protected ArrayList<WebAlbumInfo> mWebAlbumVideoList;
    protected int mBrowseType;
    protected int mType;
    private String mFormatString;

    protected static final int MEDIA_TYPE_IMAGE = 0x7001;
    protected static final int MEDIA_TYPE_VIDEO = 0x7002;

    protected static final int BROWSE_MEDIA_LOCAL = 0x8001;
    protected static final int BROWSE_MEDIA_WEBALBUM = 0x8002;
    protected static final int BROWSE_MEDIA_ALL = 0x8003;


    protected static class WebAlbumInfo {
        String thumbpath;
        String imagepath;
    }

/*    protected static class MediaItem {
        String thumb;
        String path;
    }*/

    protected AsyncHttpResponseHandler mHttpHandler = new AsyncHttpResponseHandler() {
        public void onStart() {
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            Log.i(TAG, new String(responseBody));
/*            ByteArrayInputStream is = new ByteArrayInputStream(responseBody);
            if(mInfo.getWebAlbumInfo(is)) {
                View layout = findViewById(R.id.webalbum_status_layout);
                layout.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                //Log.i(TAG, " WEBALBUM_LOAD_SUCCESS, do rendering webalbum now");
                ((GridView) listView).setAdapter(new WebAlbumImageAdapter());
            } else {
                TextView status = (TextView) findViewById(R.id.webalbum_status_text);
                status.setText(getResources().getString(R.string.action_fetch_webalbum_failure));
            }*/
            ByteArrayInputStream is = new ByteArrayInputStream(responseBody);
            try {
                XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = parserFactory.newPullParser();
                parser.setInput(is, "utf-8");
                //parser.set
                int type = parser.getEventType();
                while (type != XmlPullParser.END_DOCUMENT) {
                    switch (type) {
                        case XmlPullParser.START_DOCUMENT:
                            //Log.i(TAG, "Start Document");
                            break;
                        case XmlPullParser.START_TAG:
                            //Log.i(TAG, "start tag = " + parser.getName());
                            if (parser.getName().equals("item")) {
                                WebAlbumInfo waItem = new WebAlbumInfo();
                                String mime = parser.getAttributeValue(0);
                                waItem.thumbpath = parser.getAttributeValue(1);
                                waItem.imagepath = parser.getAttributeValue(2);

                                if(mime.startsWith("image/")) {
                                    mWebAlbumImageList.add(waItem);
                                } else {
                                    mWebAlbumVideoList.add(waItem);
                                }

                                //mWebAlbumInfo.add(waItem);
                                Log.i(TAG, " mime= " + mime + " thumb= " + waItem.thumbpath + " image= " + waItem.imagepath);
                            }
                            break;
                        case XmlPullParser.END_TAG:
                            //Log.i(TAG, "end tag = " + parser.getName());
                            break;
                        case XmlPullParser.TEXT:
                            break;
                    }
                    parser.next();
                    type = parser.getEventType();
                    //displayMediaInfo();
                }
                //mInfoHeader.setText(String.format(mFormatString, ));
                mInfoHeader.setText(String.format(mFormatString, mType==MEDIA_TYPE_IMAGE? getImageCount(): getVideoCount()));
            } catch (XmlPullParserException e) {
                Log.i(TAG, " XmlPullParserException ");
            } catch (IOException e) {
                Log.i(TAG, " IOException ");
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Log.i(TAG, " statusCode = " + statusCode);
/*            TextView status = (TextView) findViewById(R.id.webalbum_status_text);
            ProgressBar bar = (ProgressBar) findViewById(R.id.webalbum_status_progressbar);

            if(statusCode == 404) {
                status.setText(getResources().getString(R.string.text_webalbum_404));
            } else {
                status.setText(getResources().getString(R.string.action_fetch_webalbum_failure));
            }
            bar.setVisibility(View.INVISIBLE);*/
        }
    };

    public MediaBrowserActivity() {
        mHttpClient = new AsyncHttpClient();
        mWebAlbumImageList = new ArrayList<WebAlbumInfo>();
        mWebAlbumVideoList = new ArrayList<WebAlbumInfo>();
    }

    private void init_UIL() {
        ImageDecoder smartUriDecoder = new SmartUriDecoder(mContext.getContentResolver(), new BaseImageDecoder(false));

        mConfig = new ImageLoaderConfiguration.Builder(mContext)
                .denyCacheImageMultipleSizesInMemory()
                .taskExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                .imageDecoder(smartUriDecoder)
                .threadPoolSize(5)
                .taskExecutorForCachedImages(AsyncTask.THREAD_POOL_EXECUTOR)
                .threadPriority(5)
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                .memoryCache(new WeakMemoryCache())
                .memoryCacheSize(2 * 1024 * 1024)
                .memoryCacheSizePercentage(32)
                .imageDownloader(new BaseImageDownloader(mContext))
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .build();

        //mConfig = ImageLoaderConfiguration.createDefault(mContext);
        mOption = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new SimpleBitmapDisplayer())
                .build();
        imgLoader.init(mConfig);
    }

    protected void displayImage(int pos, ImageView imageView, DisplayImageOptions options, SimpleImageLoadingListener listener) {
        if(mBrowseType == BROWSE_MEDIA_ALL || mBrowseType == BROWSE_MEDIA_LOCAL) {
            Log.i(TAG, " displayImage mType");
            if (pos < mInfo.getImageCount()) {
                Log.i(TAG, "display local media " + pos + " id " + mInfo.getImageId(pos));
                imgLoader.displayImage("image/" + mInfo.getImageId(pos), imageView, options, listener);
                return;
            }
        }

        if(mBrowseType == BROWSE_MEDIA_ALL || mBrowseType == BROWSE_MEDIA_WEBALBUM) {
            int offset = 0;
            if(mBrowseType == BROWSE_MEDIA_ALL)
                offset = mInfo.getImageCount();

            if(mWebAlbumImageList.size() > 0) {
                String thumb = "http://101.69.230.238:8081/" + mWebAlbumImageList.get(pos - offset).thumbpath;
                Log.i(TAG, "display web media " + pos + " " + thumb);
                imgLoader.displayImage(thumb, imageView, options, listener);
            }
        }
    }

    protected void displayVideo(int pos, ImageView imageView, DisplayImageOptions options, SimpleImageLoadingListener listener) {
        if(mBrowseType == BROWSE_MEDIA_ALL || mBrowseType == BROWSE_MEDIA_LOCAL) {
            if (pos < mInfo.getVideoCount()) {
                imgLoader.displayImage("image/" + mInfo.getVideoId(pos), imageView, options, listener);
                return;
            }
        }
        if(mBrowseType == BROWSE_MEDIA_ALL || mBrowseType == BROWSE_MEDIA_WEBALBUM) {
            int offset = 0;
            if(mBrowseType == BROWSE_MEDIA_ALL)
                offset = mInfo.getVideoCount();

            if(mWebAlbumVideoList.size() > 0) {
                String thumb = "http://101.69.230.238:8081/" + mWebAlbumVideoList.get(pos - offset).thumbpath;
                Log.i(TAG, "display webalbum video media " + pos + " " + thumb);
                imgLoader.displayImage(thumb, imageView, options, listener);
            }
        }
    }


    protected void onCreate(Bundle saved) {
        super.onCreate(saved);

        mContext = getBaseContext();
        init_UIL();
        mPref = this.getSharedPreferences("wowyun-device", Context.MODE_PRIVATE);

        mInfo = new MediaInfo(mContext.getContentResolver());
        setContentView(R.layout.activity_media_browser);
        mInfoHeader = (TextView) findViewById(R.id.mediaview_infoheader);
        mMediaView = (GridView) findViewById(R.id.media_gridview);
        mMediaView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        //mInfo.getImagesInfo(mPref);
    }

    protected void onDestroy() {
        mHttpClient.cancelAllRequests(true);
        super.onDestroy();

    }
    protected int getImageCount() {
        int count = 0;

        switch (mBrowseType) {
            case BROWSE_MEDIA_ALL:
                count += mWebAlbumImageList.size();
            case BROWSE_MEDIA_LOCAL:
                count += mInfo.getImageCount();
            break;

            case BROWSE_MEDIA_WEBALBUM:
                count += mWebAlbumImageList.size();
                break;
        }
        Log.i(TAG, " image count " + count);
        return count;
    }

    protected int getVideoCount() {
        int count = 0;

        switch (mBrowseType) {
            case BROWSE_MEDIA_ALL:
                count += mWebAlbumVideoList.size();
            case BROWSE_MEDIA_LOCAL:
                count += mInfo.getVideoCount();
                break;

            case BROWSE_MEDIA_WEBALBUM:
                count += mWebAlbumVideoList.size();
                break;
        }

        Log.i(TAG, " video count " + count);
        return count;
    }

    protected void displayMediaInfo(int sid, int count) {
        mFormatString = getResources().getString(sid);
        //= String.format(mFormatString, count);
        mInfoHeader.setText(String.format(mFormatString, count));
    }
}
