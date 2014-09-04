package com.wowfly.wowyun.wowyun_device;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by user on 9/3/14.
 */
public class ImageViewer extends Activity {
    private ImageViewerAdapter adapter;
    private ViewPager viewPager;
    private MediaInfo mInfo = null;
    private SharedPreferences mPref;

    protected void onCreate(Bundle saved) {
        super.onCreate(saved);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_fullscreen_view);
        viewPager = (ViewPager) findViewById(R.id.imageviewer);
        viewPager.setPageTransformer(true, new DepthPageTransformer());

        Intent i = getIntent();
        int pos = i.getIntExtra("position", 0);

        mPref = getSharedPreferences("wowyun-device", Context.MODE_PRIVATE);

        mInfo = new MediaInfo(this.getContentResolver());
        mInfo.getImagesInfo(mPref);

        adapter = new ImageViewerAdapter(ImageViewer.this, mInfo);
        viewPager.setAdapter(adapter);

        viewPager.setCurrentItem(pos);
        //viewPager.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        //viewPager.setAlpha();
    }

    private class ImageViewerAdapter extends PagerAdapter {
        private static final String TAG = "ImageViewerAdapter";
        private Activity _activity;
        private LayoutInflater inflater;
        private MediaInfo mInfo;
        private Bitmap mBitmap = null;

        public ImageViewerAdapter(Activity activity, MediaInfo mi) {
            this._activity = activity;
            mInfo = mi;
        }

        public int getCount() {
            return mInfo.getImageCount();//.get._imageList.size();
        }

        public boolean isViewFromObject(View view, Object obj) {
            return view == ((RelativeLayout) obj);
        }

        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView;
            TextView tips, filename;

            if(mBitmap != null) {
                System.gc();
                mBitmap.recycle();
                mBitmap = null;
            }
            inflater = (LayoutInflater)_activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewLayout = inflater.inflate(R.layout.layout_imageviewer, container, false);
            imageView = (ImageView) viewLayout.findViewById(R.id.imageholder);

            filename = (TextView) viewLayout.findViewById(R.id.imagefilename);
            filename.setText(mInfo.getImageName(position));

            tips = (TextView) viewLayout.findViewById(R.id.imagetips);
            tips.setText((position+1) + "/" + mInfo.getImageCount());

            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
            opt.inSampleSize = 2;
            Bitmap bitmap = null;

            try {
                bitmap = BitmapFactory.decodeFile(mInfo.getImagePath(position), opt);
                imageView.setImageBitmap(bitmap);
            } catch(OutOfMemoryError e0) {
                System.gc();
                try {
                    bitmap = BitmapFactory.decodeFile(mInfo.getImagePath(position), opt);
                    imageView.setImageBitmap(bitmap);
                } catch (OutOfMemoryError e1) {
                    Log.e(TAG, "Bitmap decode really out of memory");
                }
            }

            ((ViewPager) container).addView(viewLayout);
            return  viewLayout;
        }

        public void destroyItem(ViewGroup container, int position, Object obj) {
            ((ViewPager)container).removeView((RelativeLayout) obj);
        }
    }
}
