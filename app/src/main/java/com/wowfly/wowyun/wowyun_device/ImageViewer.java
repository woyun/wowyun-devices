package com.wowfly.wowyun.wowyun_device;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MyViewPager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.BaseInputConnection;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by user on 9/3/14.
 */
public class ImageViewer extends Activity {
    private static final String TAG = "ImageViewer";
    private ImageViewerAdapter adapter;
    private ViewPager viewPager;
    private MediaInfo mInfo = null;
    private SharedPreferences mPref;
    protected ArrayList<WowYunApp.WebAlbumInfo> mWebAlbumImageList;
    private int mBrowseType;
    private Handler mHandler;
    public static final int IMAGEVIEWER_SLIDE_START = 0x1001;
    public static final int IMAGEVIEWER_SLIDE_END = 0x1002;

    public static final int IMAGEVIEW_START_LOADING = 0x2001;
    public static final int IMAGEVIEW_STOP_LOADING = 0x2002;
    private Timer timer = new Timer();
    private long mTimeTag = 0;
    private boolean bLoading = false;

    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }


    protected void onCreate(Bundle saved) {
        WowYunApp _app = (WowYunApp) getApplication();
        super.onCreate(saved);
        final int count;

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_fullscreen_view);
        viewPager = (ViewPager) findViewById(R.id.imageviewer);
        viewPager.setPageTransformer(true, new DepthPageTransformer());

        Intent i = getIntent();
        int pos = i.getIntExtra("position", 0);
        mBrowseType = i.getIntExtra("browsetype", WowYunApp.BROWSE_MEDIA_ALL);

        Log.i(TAG, " position " + pos + " browsetype " + mBrowseType);
        mPref = getSharedPreferences("wowyun-device", Context.MODE_PRIVATE);

        mInfo = new MediaInfo(this.getContentResolver());
        mInfo.getImagesInfo(mPref);

        mWebAlbumImageList = _app.getWebAlbumInfo(true);
        adapter = new ImageViewerAdapter(ImageViewer.this, mInfo, mBrowseType);
        viewPager.setAdapter(adapter);
        count = adapter.getCount();

        viewPager.setCurrentItem(pos);
        //viewPager.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        //viewPager.setAlpha();

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case IMAGEVIEW_START_LOADING:
                        bLoading = true;
                        break;

                    case IMAGEVIEW_STOP_LOADING:
                        bLoading = false;
                        mTimeTag = System.currentTimeMillis();
                        break;

                    case IMAGEVIEWER_SLIDE_START:
                        //View view = findViewById(android.R.id.content);
                        //BaseInputConnection  mInputConnection = new BaseInputConnection(view, true);
                        //mInputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT));
                        if(bLoading == true)
                            break;

                        Log.i(TAG, " goto next image by timer " + count + " " + viewPager.getCurrentItem());
                        if(viewPager.getCurrentItem()+1 >= count) {
                            viewPager.setCurrentItem(0);
                        } else {
                            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                        }
                        break;
                    case IMAGEVIEWER_SLIDE_END:
                        break;
                }

            }
        };
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                long curr = System.currentTimeMillis();
                if(curr-mTimeTag >= 30000) {
                    Log.i(TAG, "timer condition meeted, send slider start event");
                    Message msg = new Message();
                    msg.what = IMAGEVIEWER_SLIDE_START;
                    mHandler.sendMessage(msg);
                }
            }
        };
        timer.scheduleAtFixedRate(task, 3000, 3000);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        mTimeTag = System.currentTimeMillis();
        return super.onKeyDown(keyCode, event);
    }

    private class ImageLoadingAsyncHttpResponseHandler extends AsyncHttpResponseHandler {
        private ImageView mImageView;
        private ProgressBar mProgressBar;
        private TextView mTextView;

        public ImageLoadingAsyncHttpResponseHandler(ImageView imageView, ProgressBar progressBar, TextView textView) {
            mImageView = imageView;
            mProgressBar = progressBar;
            mTextView = textView;
        }

        //public void
        public void onStart() {
            //mProgressBar.set
            mProgressBar.setIndeterminate(false);
            //mProgressBar.set
            //mProgressBar.setProgress(0);
            mTextView.setText("0%");
            bLoading = true;
        }

        public void onProgress(int pos, int len) {
            //Log.i(TAG, " onProgress pos " + pos + " len " + len);
            Double p0 = new Double(pos);
            Double p1 = new Double(len);

            p0 = (p0/p1)*100;

            //mProgressBar.setProgress(p0.intValue());
            mTextView.setText(Integer.toString(p0.intValue()) + "%");
        }

        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            Bitmap bm = null;
            bLoading = false;
            mTimeTag = System.currentTimeMillis();
            mProgressBar.setVisibility(View.GONE);
            ByteArrayInputStream is = new ByteArrayInputStream(responseBody);
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.RGB_565;
            //opt.inSampleSize = 2;
            opt.inDither = true;
            Rect _rt = new Rect();
            try {
                bm = BitmapFactory.decodeStream(is, _rt, opt);
                mImageView.setImageBitmap(bm);
            } catch (OutOfMemoryError e0) {
                System.gc();
                try {
                    opt.inSampleSize = 2;
                    bm = BitmapFactory.decodeStream(is, _rt, opt);
                    mImageView.setImageBitmap(bm);
                    //Log.i(TAG, " load bitmap " + mInfo.getImagePath(position) + " h " + bitmap.getHeight() + " w " + bitmap.getWidth());
                    //mAttacher.update();
                } catch (OutOfMemoryError e1) {
                    Log.e(TAG, "Bitmap decode really out of memory");
                }
            }
            Log.i(TAG, " ImageLoadingAsyncHttpResponseHandler Success");
            mTextView.setVisibility(View.GONE);
        }
        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            mProgressBar.setVisibility(View.GONE);
            mTextView.setVisibility(View.GONE);
            Log.i(TAG, " ImageLoadingAsyncHttpResponseHandler Failure");
        }
    }

    private class ImageViewerAdapter extends PagerAdapter {
        private static final String TAG = "ImageViewerAdapter";
        private Activity _activity;
        private LayoutInflater inflater;
        private MediaInfo mInfo;
        private Bitmap mBitmap = null;
        //private PhotoViewAttacher mAttacher;
        private boolean isWebAlbum = false;
        private int mBrowseType;
        private ProgressBar loadingProgressBar;
        private AsyncHttpClient mHttpClient;
        private int mScreenWidth;
        private int mScreenHeight;
        private static final int HTTP_CLIENT_OBJ = 0x8001;

        public ImageViewerAdapter(Activity activity, MediaInfo mi, int browseType) {
            this._activity = activity;
            mInfo = mi;
            mBrowseType = browseType;
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            mScreenHeight = displaymetrics.heightPixels;
            mScreenWidth = displaymetrics.heightPixels;
            mHttpClient = new AsyncHttpClient();
        }

        public int getCount() {
            if(mBrowseType == WowYunApp.BROWSE_MEDIA_LOCAL)
                return mInfo.getImageCount();
            else if(mBrowseType == WowYunApp.BROWSE_MEDIA_WEBALBUM)
                return mWebAlbumImageList.size();
            else
                return mInfo.getImageCount() + mWebAlbumImageList.size();
        }

        public boolean isViewFromObject(View view, Object obj) {
            return view == ((RelativeLayout) obj);
        }

        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView;
            TextView tips, filename, progressText;
            boolean isLocalImage = false;
            int count = mInfo.getImageCount();

            if(mBitmap != null) {
                System.gc();
                mBitmap.recycle();
                mBitmap = null;
            }
            inflater = (LayoutInflater)_activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewLayout = inflater.inflate(R.layout.layout_imageviewer, container, false);
            imageView = (ImageView) viewLayout.findViewById(R.id.imageholder);
            progressText = (TextView) viewLayout.findViewById(R.id.image_loading_text);
/*            mAttacher = new PhotoViewAttacher(imageView);
            mAttacher.setMinimumScale(0.0f);
            mAttacher.setMaximumScale(100.0f);*/
            //mAttacher
            //mAttacher.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            //mAttacher.setScaleType();
            //mAttacher.setZoomable(true);
            //mAttacher.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //mAttacher.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            filename = (TextView) viewLayout.findViewById(R.id.imagefilename);
            tips = (TextView) viewLayout.findViewById(R.id.imagetips);
            loadingProgressBar = (ProgressBar) viewLayout.findViewById(R.id.image_loading_progressbar);

            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.RGB_565;
            //opt.inSampleSize = 2;
            opt.inDither = true;
            Bitmap bitmap = null;

            if(mBrowseType == WowYunApp.BROWSE_MEDIA_LOCAL) {
                isLocalImage = true;
            } else if(mBrowseType == WowYunApp.BROWSE_MEDIA_ALL && position < mInfo.getImageCount()) {
                isLocalImage = true;
                count += mWebAlbumImageList.size();
            } else {
                count = mWebAlbumImageList.size();
            }

            tips.setText((position+1) + "/" + count);

            if(isLocalImage) {
                filename.setText(mInfo.getImageName(position));
                loadingProgressBar.setVisibility(View.GONE);
                try {
                    bitmap = BitmapFactory.decodeFile(mInfo.getImagePath(position), opt);
                    //Log.i(TAG, " load bitmap " + mInfo.getImagePath(position) + " h " + bitmap.getHeight() + " w " + bitmap.getWidth());
                    imageView.setImageBitmap(bitmap);
                    //mAttacher.update();
                } catch (OutOfMemoryError e0) {
                    System.gc();
                    try {
                        opt.inSampleSize = 2;
                        bitmap = BitmapFactory.decodeFile(mInfo.getImagePath(position), opt);
                        //Log.i(TAG, " load bitmap " + mInfo.getImagePath(position) + " h " + bitmap.getHeight() + " w " + bitmap.getWidth());
                        imageView.setImageBitmap(bitmap);
                        //mAttacher.update();
                    } catch (OutOfMemoryError e1) {
                        Log.e(TAG, "Bitmap decode really out of memory");
                    }
                }
            } else {
                int _pos = position;
                if(mBrowseType == WowYunApp.BROWSE_MEDIA_ALL) {
                    _pos = position - mInfo.getImageCount();
                }

                String url = mWebAlbumImageList.get(_pos).imagepath;
                File imageCache = new File(getFilesDir(), url.replace('/', '_'));
                if(imageCache.exists()) {
                    System.gc();
                    Log.i(TAG, " image " + imageCache.getAbsolutePath() + " exists " + imageCache.getPath());
                    loadingProgressBar.setVisibility(View.GONE);
                    try {
                        bitmap = BitmapFactory.decodeFile(imageCache.getPath(), opt);
                        //bitmap = BitmapFactory.decodeFile("/data/data/d1081428935_admin_IMG_20140808_065936.jpg", opt);
                        //bitmap = BitmapFactory.decodeFile()
                        Log.i(TAG, " load bitmap " + " h " + bitmap.getHeight() + " w " + bitmap.getWidth());
                        imageView.setImageBitmap(bitmap);
                    } catch (OutOfMemoryError e0) {
                        System.gc();
                        try {
                            opt.inSampleSize = 2;
                            bitmap = BitmapFactory.decodeFile(imageCache.getAbsolutePath(), opt);
                            Log.i(TAG, " load * bitmap " + " h " + bitmap.getHeight() + " w " + bitmap.getWidth());
                            imageView.setImageBitmap(bitmap);
                            //mAttacher.update();
                        } catch (OutOfMemoryError e1) {
                            Log.e(TAG, "Bitmap decode really out of memory");
                        }
                    }
                    return viewLayout;
                }

                int sep = url.lastIndexOf('/');
                String imagename = url.substring(sep+1);
                filename.setText(imagename);
                //String mime = mInfo.getWebAlbumMediaMime(position);
                Log.i(TAG, " start web album viewer " + url  + "  pos " + position);
                url = "http://101.69.230.238:8081/" + url;
                //new DownloadImageTask((ImageView) viewLayout.findViewById(R.id.imageholder)).execute(url);
                ImageLoadingAsyncHttpResponseHandler mHttpHandler = new ImageLoadingAsyncHttpResponseHandler(imageView, loadingProgressBar, progressText);
                mHttpClient.get(url, mHttpHandler);
                viewLayout.setTag(R.id.httpclient, mHttpClient);
            }

            ((ViewPager) container).addView(viewLayout);
            return  viewLayout;
        }

        public void destroyItem(ViewGroup container, int position, Object obj) {
            RelativeLayout viewLayout = (RelativeLayout) obj;
            AsyncHttpClient httpClient = (AsyncHttpClient) viewLayout.getTag(R.id.httpclient);
            if(httpClient != null) {
                //Log.i(TAG, "try to release all http client requests");
                //httpClient.cancelAllRequests(true);
            }
            ((ViewPager)container).removeView((RelativeLayout) obj);
        }
    }
}
