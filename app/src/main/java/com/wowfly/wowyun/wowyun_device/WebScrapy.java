package com.wowfly.wowyun.wowyun_device;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.jsoup.nodes.Element;

import java.util.ArrayList;

/**
 * Created by user on 9/15/14.
 */
abstract public class WebScrapy {
    private static final String TAG = "WebScrapy";

    interface ScrapyCallback {
        void onSuccess(int statusCode, Header[] headers, byte[] resp);
        void onFailure(int statusCode, Header[] headers, byte[] resp);
    }

    protected class ElementInfo {
        String date;
        String username;
        Element element;
        long created_time;
    }
    protected String loginURI;
    protected AsyncHttpClient mHttpClient = new AsyncHttpClient();
    private ScrapyCallback mCallback;
    protected int status;
    protected Handler mHandler;
    protected WowPersistentCookieStore mCookieStore;
    protected Context mContext;
    protected int mCount = 0;
    protected ArrayList<ElementInfo> mElemList;

    final static int STATUS_INIT = 0x1000;
    final static int STATUS_PREPARE_LOGIN = 0x1001;
    final static int STATUS_DOLOGIN = 0x1002;
    final static int STATUS_LOGIN_SUCCESS = 0x1003;
    final static int STATUS_GET_PROFILE=0x1005;
    final static int STATUS_GET_WEIBO=0x1006;
    final static int STATUS_GET_VC = 0x1007;

    final static int SNS_TYPE_WEIBO = 0x2000;
    final static int SNS_TYPE_QZONE = 0x2001;

    final static int SNS_INFO_UPDATE = 0x9001;

    final static int ACTION_BIND = 0x6000;

    protected String mUSERNAME;
    protected String mPASSWORD;
    protected String mProfileImageURL;
    protected String mNICKNAME;

    protected int actionMode;

    protected AsyncHttpResponseHandler mRespHandler = new AsyncHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            if(mCallback != null)
                mCallback.onSuccess(statusCode, headers, responseBody);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            if(mCallback != null)
                mCallback.onFailure(statusCode, headers, responseBody);
        }
    };

    public WebScrapy() {
        status = STATUS_INIT;
    }

    public void setScrapyCallback(ScrapyCallback callback) {
        mCallback = callback;
    }

    public void doInit() {
        Log.i(TAG, " WebScrapy.doInit " + loginURI );
        if(loginURI != null) {
            mHttpClient.get(loginURI, mRespHandler);
        }
    }

    abstract void doLogin(String username, String password);
    abstract void setVC(String vc);

    public ArrayList<ElementInfo> getElemList() {
        return mElemList;
    }

    public int getElemCount() {
        return mCount;
    }

/*    public void doLogin() {
        //mHttpClient.get(loginURI, mRespHandler);
    }*/

    //abstract void doInit();
    //abstract void doPrepareLogin();
    //abstract void doLogin();
    //abstract void
}
