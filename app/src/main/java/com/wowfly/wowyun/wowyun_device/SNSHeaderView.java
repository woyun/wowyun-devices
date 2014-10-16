package com.wowfly.wowyun.wowyun_device;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by user on 9/15/14.
 */
public class SNSHeaderView extends ImageView {
    private Context mContext;
    private static final String TAG = "SNSHeaderView";
    private ArrayList<SNSPerson> mPersonList;
    private Paint mPaint;
    private PaintFlagsDrawFilter pfd;
    private SNSHeaderListener headerListener;
    private int nHeight = 32;
    private int nSelected = 0;
    private int padding = 0;
    private int maxIconWidth = 0;

    interface SNSHeaderListener {
        void onSelected(int pos);
    }
    public SNSHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mPersonList = new ArrayList<SNSPerson>();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        pfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
    }

    public SNSPerson getCurrentSNSPerson() {
        if(mPersonList.size() > 0)
            return mPersonList.get(nSelected);
        else
            return null;
    }

    public void addSNSPerson(SNSPerson p) {
        mPersonList.add(p);
        maxIconWidth = p.getIcon().getWidth();
    }

    public void addHeaderListener(SNSHeaderListener listener) {
        headerListener = listener;
    }

    private void drawSelectRect(Canvas canvas, float x0, float y0, float right, float bottom) {
        Paint circlePaint = new Paint();
        circlePaint.setColor(getResources().getColor(android.R.color.holo_blue_dark));
        //circlePaint.set
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(3.6f);
        canvas.drawRect(x0, y0+2.0f, right, bottom-8.0f, circlePaint);
    }

    protected void onDraw(Canvas canvas) {
        Matrix mx = new Matrix();
        super.onDraw(canvas);
        canvas.setDrawFilter(pfd);
        int height = getHeight(), width = getWidth();
        Log.i(TAG, " SNSHeaderView " + height + " width " + width);
        float x0 = (width - ((mPersonList.size()*maxIconWidth) + padding*(mPersonList.size()-1)))/2.0f;
        float y0 = 0.0f;
        float rx = x0 + nSelected*maxIconWidth + padding;
        float ry = y0;
        float rr = rx + maxIconWidth;
        float rb = ry + maxIconWidth;

        for(int idx=0; idx<mPersonList.size(); idx++) {
            Log.i(TAG, " " + mPersonList.get(idx).getName());
            //mx.setTranslate(idx*200.0f, 0.0f);
            //canvas.drawBitmap(mPersonList.get(idx).getIcon(), mx, mPaint);
            canvas.drawBitmap(mPersonList.get(idx).getIcon(), x0, y0, mPaint);
            x0 = x0 + maxIconWidth + padding;
        }
        drawSelectRect(canvas, rx, ry, rr, rb);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i(TAG, " widthMeasureSpec " + widthMeasureSpec + " heightMeasureSpec " + heightMeasureSpec);
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));

        int childWidthSize = getMeasuredWidth();
        int childHeightSize = 138;//getMeasuredHeight();
        Log.i(TAG, " childWidthSize " + childWidthSize + " childHeightSize " + childHeightSize);

        heightMeasureSpec = widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeightSize, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void gotoNextIcon() {
        nSelected = (nSelected + 1)%mPersonList.size();
        invalidate();
    }

    public void gotoPrevIcon() {
        nSelected = nSelected -1;
        if(nSelected < 0)
            nSelected = mPersonList.size()-1;
        invalidate();
    }

/*    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            nSelected = (nSelected + 1)%mPersonList.size();
            invalidate();
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            nSelected = nSelected -1;
            if(nSelected < 0)
                nSelected = mPersonList.size()-1;
            invalidate();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }*/
}
