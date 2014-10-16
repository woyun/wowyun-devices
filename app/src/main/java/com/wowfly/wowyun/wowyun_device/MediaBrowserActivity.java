package com.wowfly.wowyun.wowyun_device;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.BaseAdapter;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    protected ArrayList<WowYunApp.WebAlbumInfo> mWebAlbumImageList;
    protected ArrayList<WowYunApp.WebAlbumInfo> mWebAlbumVideoList;
    protected int mBrowseType;
    protected int mType;
    private String mFormatString;

    protected static final int MEDIA_TYPE_IMAGE = 0x7001;
    protected static final int MEDIA_TYPE_VIDEO = 0x7002;

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
                                WowYunApp.WebAlbumInfo waItem = new WowYunApp.WebAlbumInfo();
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
                //mMediaView.setSelection(0);
                //mMediaView.setSelected(true);
                BaseAdapter adapter = (BaseAdapter) mMediaView.getAdapter();
                adapter.notifyDataSetInvalidated();

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
        if(mBrowseType == WowYunApp.BROWSE_MEDIA_ALL || mBrowseType == WowYunApp.BROWSE_MEDIA_LOCAL) {
            Log.i(TAG, " displayImage mType");
            if (pos < mInfo.getImageCount()) {
                Log.i(TAG, "display local media " + pos + " id " + mInfo.getImageId(pos));
                imgLoader.displayImage("image/" + mInfo.getImageId(pos), imageView, options, listener);
                return;
            }
        }

        if(mBrowseType == WowYunApp.BROWSE_MEDIA_ALL || mBrowseType == WowYunApp.BROWSE_MEDIA_WEBALBUM) {
            int offset = 0;
            if(mBrowseType == WowYunApp.BROWSE_MEDIA_ALL)
                offset = mInfo.getImageCount();

            if(mWebAlbumImageList.size() > 0) {
                String thumb = "http://101.69.230.238:8081/" + mWebAlbumImageList.get(pos - offset).thumbpath;
                Log.i(TAG, "display web media " + pos + " " + thumb);
                imgLoader.displayImage(thumb, imageView, options, listener);
            }
        }
    }

    protected void displayVideo(int pos, ImageView imageView, DisplayImageOptions options, SimpleImageLoadingListener listener) {
        if(mBrowseType == WowYunApp.BROWSE_MEDIA_ALL || mBrowseType == WowYunApp.BROWSE_MEDIA_LOCAL) {
            if (pos < mInfo.getVideoCount()) {
                imgLoader.displayImage("image/" + mInfo.getVideoId(pos), imageView, options, listener);
                return;
            }
        }
        if(mBrowseType == WowYunApp.BROWSE_MEDIA_ALL || mBrowseType == WowYunApp.BROWSE_MEDIA_WEBALBUM) {
            int offset = 0;
            if(mBrowseType == WowYunApp.BROWSE_MEDIA_ALL)
                offset = mInfo.getVideoCount();

            if(mWebAlbumVideoList.size() > 0) {
                String thumb = "http://101.69.230.238:8081/" + mWebAlbumVideoList.get(pos - offset).thumbpath;
                Log.i(TAG, "display webalbum video media " + pos + " " + thumb);
                imgLoader.displayImage(thumb, imageView, options, listener);
            }
        }
    }


    protected String getMediaUriByPosition(int pos) {
        if(mBrowseType == WowYunApp.BROWSE_MEDIA_ALL || mBrowseType == WowYunApp.BROWSE_MEDIA_LOCAL) {
            if (pos < mInfo.getVideoCount()) {
                return mInfo.getVideoUri(pos);
            }
        }
        if(mBrowseType == WowYunApp.BROWSE_MEDIA_ALL || mBrowseType == WowYunApp.BROWSE_MEDIA_WEBALBUM) {
            int offset = 0;
            if(mBrowseType == WowYunApp.BROWSE_MEDIA_ALL)
                offset = mInfo.getVideoCount();

            if(mWebAlbumVideoList.size() > 0) {
                String path = "http://101.69.230.238:8081/" + mWebAlbumVideoList.get(pos - offset).imagepath;
                Log.i(TAG, "display webalbum video media " + pos + " " + path);
                //imgLoader.displayImage(thumb, imageView, options, listener);
                return path;
            }
        }
        return null;
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
        //mMediaView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        //mInfo.getImagesInfo(mPref);
        WowYunApp _app = (WowYunApp) getApplication();
        mWebAlbumImageList = _app.getWebAlbumInfo(true);
        mWebAlbumVideoList = _app.getWebAlbumInfo(false);
        Log.i(TAG, "_app = " + _app);

        mWebAlbumImageList.clear();
        mWebAlbumVideoList.clear();
    }

    protected void onDestroy() {
        mHttpClient.cancelAllRequests(true);
        super.onDestroy();

    }

    protected void showBrowseTypeSelectionDialog(Activity activity, int title_sid,  int opt_sid) {
        final AlertDialog dlg;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(getResources().getString(title_sid));
        //builder.setSingleChoiceItems()
        builder.setSingleChoiceItems(opt_sid, mBrowseType - WowYunApp.BROWSE_MEDIA_ALL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Log.i(TAG, " select item = " + i);
                switch (i) {
                    case 0:
                        mBrowseType = WowYunApp.BROWSE_MEDIA_ALL;
                        break;
                    case 1:
                        mBrowseType = WowYunApp.BROWSE_MEDIA_WEBALBUM;
                        break;
                    case 2:
                        mBrowseType = WowYunApp.BROWSE_MEDIA_LOCAL;
                        break;
                }
                mInfoHeader.setText(String.format(mFormatString, mType == MEDIA_TYPE_IMAGE ? getImageCount() : getVideoCount()));
                BaseAdapter adapter = (BaseAdapter) mMediaView.getAdapter();
                adapter.notifyDataSetInvalidated();

                dialogInterface.cancel();
            }
        });
        dlg = builder.create();
        dlg.show();

/*        List<Map<String, Object>> optionList = new ArrayList<Map<String, Object>>();
        Map<String, Object> item = new HashMap<String, Object>();

        item.put("name", getResources().getString(R.string.dialog_buddy_option2));
        item.put("details", getResources().getString(R.string.dialog_buddy_option2_details));
        optionList.add(item);

        item = new HashMap<String, Object>();
        //item.put("icon", R.drawable.ic_option_icon_setting);
        item.put("name", getResources().getString(R.string.dialog_buddy_option3));
        item.put("details", getResources().getString(R.string.dialog_buddy_option3_details));
        optionList.add(item);*/
    }

/*    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                showBrowseTypeSelectionDialog();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }*/

    protected int getImageCount() {
        int count = 0;

        switch (mBrowseType) {
            case WowYunApp.BROWSE_MEDIA_ALL:
                count += mWebAlbumImageList.size();
            case WowYunApp.BROWSE_MEDIA_LOCAL:
                count += mInfo.getImageCount();
            break;

            case WowYunApp.BROWSE_MEDIA_WEBALBUM:
                count += mWebAlbumImageList.size();
                break;
        }
        Log.i(TAG, " image count " + count);
        return count;
    }

    protected int getVideoCount() {
        int count = 0;

        switch (mBrowseType) {
            case WowYunApp.BROWSE_MEDIA_ALL:
                count += mWebAlbumVideoList.size();
            case WowYunApp.BROWSE_MEDIA_LOCAL:
                count += mInfo.getVideoCount();
                break;

            case WowYunApp.BROWSE_MEDIA_WEBALBUM:
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
