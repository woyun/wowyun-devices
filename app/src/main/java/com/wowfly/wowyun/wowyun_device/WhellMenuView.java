package com.wowfly.wowyun.wowyun_device;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import java.lang.reflect.Field;

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by user on 8/25/14.
 */
public class WhellMenuView extends ImageView{
    private static final String TAG = "WhellMenuView";
    private Context context;
    private PaintFlagsDrawFilter pfd;
    private int nWhell, nSelected;
    private Bitmap mBackGround, mSelection;
    private Bitmap [][] mIconBitmap;
    private Matrix matrix;
    private int nScreenWidth, nScreenHeight;
    private int nBGWidth, nBGHeight;
    private Paint mPaint;
    private String[] icontext;
    private float degressDelta = 0.0f, degressOffset = 0.0f;
    private RotateAnimation rotate;
    private AnimationSet spriteAni;
    private boolean isDrawed;
    private float sDegress = 0.0f, eDegress = 0.0f;
    private Path[] mTextPath;
    private GifImageView mGif;
    //private Bitmap mNotificationIcon;
    private boolean bAnimationEnd = false;
    private Handler mHandler;
    private int[] nNotificationNumber;


    public WhellMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                //ImageView statusIcon = (ImageView)findViewById(R.id.login_status_icon);
                switch (msg.what) {
                    case WowYunApp.ANIMATION_END:
                        Log.i(TAG, " ANIMATION END, invalidate view now");
                        ((View) WhellMenuView.this).invalidate();
                        break;
                }
                super.handleMessage(msg);
            }
        };
        //this.setOnKeyListener(new WhellKeyListener());
    }

    private void init(Context context) {
        this.context = context;
        Log.i(TAG, "initialzed");
        this.setScaleType(ScaleType.MATRIX);
        nSelected = 0;
        isDrawed = false;

        if(matrix == null) {
            matrix = new Matrix();
        } else {
            matrix.reset();
        }

        mIconBitmap = new Bitmap[12][2];

        Resources res = context.getResources();
        //mVideo = new Bitmap[2];
        mIconBitmap[0][0] = BitmapFactory.decodeResource(res, R.drawable.icon_video);
        mIconBitmap[0][1] = BitmapFactory.decodeResource(res, R.drawable.icon_video_new);

        //mImage = new Bitmap[2];
        mIconBitmap[1][0] = BitmapFactory.decodeResource(res, R.drawable.icon_image);
        mIconBitmap[1][1] = BitmapFactory.decodeResource(res, R.drawable.icon_image_new);

        //mUpdate = new Bitmap[2];
        mIconBitmap[2][0] = BitmapFactory.decodeResource(res, R.drawable.icon_family);
        mIconBitmap[2][1] = BitmapFactory.decodeResource(res, R.drawable.icon_family_new);

        //mVChat = new Bitmap[2];
        mIconBitmap[3][0] = BitmapFactory.decodeResource(res, R.drawable.icon_camera);
        mIconBitmap[3][1] = BitmapFactory.decodeResource(res, R.drawable.icon_camera_new);

        //mSetting = new Bitmap[2];
        mIconBitmap[4][0] = BitmapFactory.decodeResource(res, R.drawable.icon_set);
        mIconBitmap[4][1] = BitmapFactory.decodeResource(res, R.drawable.icon_set);

        //mTools = new Bitmap[2];
        mIconBitmap[5][0] = BitmapFactory.decodeResource(res, R.drawable.icon_tool);
        mIconBitmap[5][1] = BitmapFactory.decodeResource(res, R.drawable.icon_tool);

        //mAbout = new Bitmap[2];
        mIconBitmap[6][0] = BitmapFactory.decodeResource(res, R.drawable.icon_about);
        mIconBitmap[6][1] = BitmapFactory.decodeResource(res, R.drawable.icon_about);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);

        icontext = getResources().getStringArray(R.array.icontext);

        //mNotificationIcon = BitmapFactory.decodeResource(res, R.drawable.ic_notification_circle);
    }

    public void setWhellCount(int nWhell, int w, int h) {
        Field field;
        int ID = 0;

        Log.i(TAG, "screen width = " + w + " height = " + h);
        nScreenHeight = h;
        nScreenWidth = w;

        this.nWhell = nWhell;
        degressDelta = 360.0f/nWhell;
        Log.i(TAG, "init.degressDelta = " + degressDelta);
        mTextPath = new Path[nWhell];
        for(int idx=0; idx<nWhell; idx++) {
            mTextPath[idx] = new Path();
        }

        try {
            field = R.drawable.class.getField("home_disk" + nWhell);
            ID = field.getInt(new R.drawable());
        } catch (NoSuchFieldException e) {
            Log.i(TAG, "resource is unavailable");
        } catch (IllegalAccessException e) {
            Log.i(TAG, "resource is unavailable");
        }

        mBackGround = BitmapFactory.decodeResource(context.getResources(), ID);
        this.setBackground(getResources().getDrawable(ID));

        try {
            field = R.drawable.class.getField("home_disk" + nWhell + "_opt");
            ID = field.getInt(new R.drawable());
        } catch (NoSuchFieldException e) {
            Log.i(TAG, "resource is unavailable");
        } catch (IllegalAccessException e) {
            Log.i(TAG, "resource is unavailable");
        }

        mSelection = BitmapFactory.decodeResource(context.getResources(), ID);
        startGifAnimation();

        nNotificationNumber = new int[nWhell];
        resetNotification();
    }



    public void resetNotification() {
        for(int idx=0; idx<nWhell; idx++) {
            nNotificationNumber[idx] = 0;
        }
    }

    public void setNotificationNumber(int idx, int num) {
        nNotificationNumber[idx] = num;
    }

    public void incNotificationNumber(int idx) {
        int n = nNotificationNumber[idx];
        nNotificationNumber[idx] = n +1;
        postInvalidate();
    }

    private void startGifAnimation() {
        int ID = 0;

        switch(nSelected) {
            case 0:
                ID = R.drawable.ic_video_gif;
                break;
            case 1:
                ID = R.drawable.ic_image_gif;
                break;
            case 2:
                ID = R.drawable.ic_family_gif;
                break;
            case 3:
                ID = R.drawable.ic_vchat_gif;
                break;
            case 4:
                ID = R.drawable.ic_setting_gif;
                break;
            case 5:
                ID = R.drawable.ic_tool_gif;
                break;
            case 6:
                ID = R.drawable.ic_about_gif;
                break;
        }

        if(mGif == null) {
            //mGif = (GifImageView) context.get findViewById(R.id.center_gif);
            mGif = (GifImageView)((Activity)context).getWindow().getDecorView().findViewById(R.id.center_gif);
            Log.i(TAG, "mGif = " + mGif);
        }

        if(mGif != null) {
            mGif.setImageResource(ID);
        }
        //mGif.
        //mGif.
    }

    public int getSelectedPosition() {
        return nSelected;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        nBGWidth = mBackGround.getWidth();
        nBGHeight = mBackGround.getHeight();

        Log.i(TAG, "size Changed w = " + w + " h = " + h + " oldw = " + nBGWidth + " oldh = " + nBGHeight);

        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void drawNotification(Canvas canvas, float _degress, float nR, int n) {
            Paint circlePaint = new Paint();
            circlePaint.setColor(getResources().getColor(android.R.color.holo_red_dark));
/*            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeWidth(6.0f);
            canvas.drawRect(0.0f, 0.0f, nBGWidth, nBGHeight, circlePaint);*/

            float x1 = (float) (nBGWidth / 2.0f + nR * (Math.cos((_degress - 120.0f) * 3.14 / 180.0)));
            float y1 = (float) (nBGHeight / 2.0f + nR * (Math.sin((_degress - 120.0f) * 3.14 / 180.0)));
            //Log.i(TAG, " x1= " + x1 + " y1= " + y1);

            circlePaint.setStyle(Paint.Style.FILL);
            //canvas.drawCircle(700, 10, 22.0f, circlePaint);
            canvas.drawCircle(x1, y1, 26.0f, circlePaint);

            Path _path = new Path();
            //_path.arcTo(rectF, _degress-120.0f, 20.0f, true);
            _path.addCircle(x1, y1, 26.f, Path.Direction.CW);

            Paint notifyPaint = new Paint();
            notifyPaint.setTextSize(20);
            notifyPaint.setTextAlign(Paint.Align.CENTER);
            notifyPaint.setTypeface(Typeface.DEFAULT_BOLD);
            notifyPaint.setColor(getResources().getColor(android.R.color.white));
            //canvas.drawText(Integer.toString(idx), x1, y1, notifyPaint);
            canvas.drawTextOnPath(Integer.toString(n), _path, 25.0f, 25.0f, notifyPaint);
            //http://developer.android.com/reference/android/graphics/Path.Direction.html
/*                canvas.save();
                Matrix _mx = new Matrix();

                _mx.preTranslate(nBGWidth/2.0f, nBGHeight/2.0f);

                //_mx.postTranslate(offsetW, offsetH);
                canvas.setMatrix(_mx);
                canvas.drawRect(0.0f, 0.0f, 300.0f, 200.0f, circlePaint);

                circlePaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(300.0f, 0.0f, 22.0f, circlePaint);

                Path notifyPath = new Path();
                notifyPath.arcTo(rectF, _degress-textDegressOffset + 10.0f, 48.0f, true);
                Paint notifyPaint = new Paint();
                notifyPaint.setTextSize(28);
                notifyPaint.setTextAlign(Paint.Align.CENTER);
                notifyPaint.setTypeface(Typeface.DEFAULT_BOLD);
                notifyPaint.setColor(getResources().getColor(android.R.color.white));
                notifyPaint.setStrokeWidth(1.8f);
                canvas.drawTextOnPath("212", notifyPath, offsetW - 50, offsetH - 50, notifyPaint);

                canvas.restore();*/
    }


    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Matrix mx = new Matrix();
        mPaint.setTextSize(28);
        mPaint.setTextAlign(Paint.Align.CENTER);

        //int offset = 110;
        float iconOffset = 0.18f;
        float offsetW, offsetH, textDegressOffset = 144.f + degressDelta/2.0f;

        canvas.setDrawFilter(pfd);

        //Log.i(TAG, "onDraw start width = " + nBGWidth + " height = " + nBGHeight + " nselected = " + nSelected);
        offsetH = iconOffset * nBGHeight;
        offsetW = iconOffset * nBGWidth;
        RectF rectF = new RectF(0.0f, 0.0f, nBGWidth, nBGHeight);
        float nR = 0.36f*nBGWidth;

        for (int idx = 0; idx < 7; idx++) {
            float _degress = getDegrees(idx);

            mTextPath[idx].arcTo(rectF, _degress - textDegressOffset, degressDelta, true);
            canvas.drawTextOnPath(icontext[(idx+nSelected)%nWhell], mTextPath[idx], offsetW - 84, offsetH - 84, mPaint);
            //Log.i(TAG, " icon text = " + icontext[idx] + " idx = " + idx + " _degress = " + _degress + " nSelected = " + nSelected);
            //if(idx == nSelected)
            //    continue;

            mx.setRotate(_degress, nBGWidth / 2.0f - offsetW, nBGHeight / 2.0f - offsetH);
            mx.postTranslate(offsetW, offsetH);
            canvas.drawBitmap(mIconBitmap[idx][0], mx, mPaint);

            if(nNotificationNumber[idx] > 0) {
                drawNotification(canvas, _degress, nR, nNotificationNumber[idx]);
            }
            //mx.postTranslate()
            //mx.postRotate(-10.0f, nBGWidth / 2.0f - offsetW, nBGHeight / 2.0f - offsetH);
            //canvas.drawBitmap(mNotificationIcon, mx, mPaint);
        }

        drawSelectionIcon(canvas, nSelected);
        //drawSelectionImage(canvas);
    }

    private void createAnitmaionNext() {
        rotate = new RotateAnimation(-degressDelta, 0.0f, nBGWidth / 2.0f, nBGHeight / 2.0f);
        rotate.setRepeatMode(Animation.RESTART);
        rotate.setRepeatCount(0);
        //rotate.setFillAfter(true);
        spriteAni = new AnimationSet(true);
        spriteAni.addAnimation(rotate);
        spriteAni.setDuration(360L);
        startAnimation(spriteAni);
    }

    private void createAnitmaionPrev() {
        rotate = new RotateAnimation(degressDelta , 0.0f, nBGWidth / 2.0f, nBGHeight / 2.0f);
        rotate.setRepeatMode(Animation.RESTART);
        rotate.setRepeatCount(0);
        spriteAni = new AnimationSet(true);
        spriteAni.addAnimation(rotate);
        spriteAni.setDuration(360L);
        startAnimation(spriteAni);
    }

    private void drawSelectionImage(Canvas canvas) {
        float selectedOffset = 0.009f;
        float offsetX, offsetY;
        //Matrix mx = new Matrix();

        //offsetW = selectedOffset * nBGWidth;
        //offsetH = selectedOffset * nBGHeight;
        offsetX = nBGWidth/2.0f - mSelection.getWidth()/2.0f;
        offsetY = nBGHeight*selectedOffset;

        //mx.setRotate(0.0f, nBGWidth/2.0f - offsetW, nBGHeight/2.0f - offsetH);
        //mx.postTranslate(offsetW, offsetH);
        //canvas.drawBitmap(mSelection, mx, mPaint);
        canvas.drawBitmap(mSelection, offsetX, offsetY, mPaint);
    }

    private void drawSelectionIcon(Canvas canvas, int idx) {
        float iconOffset = 0.18f;
        Matrix mx = new Matrix();
        float offsetW, offsetH;
        //Paint paint = new Paint();

        //canvas.drawBitmap(mBackGround, 0.0f, 0.0f, paint);
        //canvas.drawBitmap(mBackGround, 0.0f, 0.0f, mPaint);

        //Log.i(TAG, "onDraw start width = " + nBGWidth + " height = " + nBGHeight);
        offsetH = iconOffset*nBGHeight;
        offsetW = iconOffset*nBGWidth;

        mx.setRotate(45.0f, nBGWidth/2.0f - offsetW, nBGHeight/2.0f - offsetH);
        mx.postTranslate(offsetW, offsetH);
        canvas.drawBitmap(mIconBitmap[idx][1], mx, mPaint);
    }

    private float getDegrees(int idx) {
        float degress = (idx*360.00f)/nWhell;

        //Log.i(TAG, "idx = " + idx + " degress = " + degress + 45.0f + " degressOffset = " + degressOffset + " degressDelta = " + degressDelta);

        return degress + 45.0f + degressOffset;
    }

    public void goPrevItem() {

        //startAnimation(spriteAni);
        createAnitmaionNext();

        nSelected --;
        if(nSelected < 0) {
            nSelected = nWhell - 1;
        }
        degressOffset = degressOffset + degressDelta;
        spriteAni.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //WhellMenuView.this.invalidate();
                //WhellMenuView.this.drawSelectionIcon();
                startGifAnimation();
                Log.i(TAG, " degressOffset = " + degressOffset + " degressDelta = " + degressDelta);
                sDegress = sDegress + degressDelta;
                eDegress = sDegress + degressDelta;
                //WhellMenuView.this.invalidate();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void goNextItem() {
        //this.invalidate();
        createAnitmaionPrev();
        nSelected ++;
        if(nSelected >= nWhell) {
            nSelected = 0;
        }
        degressOffset = degressOffset - degressDelta;
        spriteAni.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                bAnimationEnd = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startGifAnimation();
                Log.i(TAG, " degressOffset = " + degressOffset + " degressDelta = " + degressDelta);
                //startAnimation(spriteAni);
                sDegress = sDegress - degressDelta;
                eDegress = sDegress + degressDelta;

                bAnimationEnd = false;
                //Message msg = new Message();
                //msg.what = WowYunApp.ANIMATION_END;
                //WhellMenuView.this.invalidate();//mHandler.sendMessage(msg);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

/*    public interface WheelChangeListener {
        *//**
         * Called when user selects a new position in the wheel menu.
         *
         * @param selectedPosition the new position selected.
         *//*
        public void onSelectionChange(int selectedPosition);
    }*/

/*    private class WhellKeyListener implements OnKeyListener {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            Log.i(TAG, "new key code = " + keyCode);
            if(keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                Log.i(TAG, "Volume up key event");
            }
            if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                Log.i(TAG, "Volume down key event");
            }

            return true;
        }
    }*/
}
