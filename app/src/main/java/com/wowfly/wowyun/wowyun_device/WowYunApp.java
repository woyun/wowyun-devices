package com.wowfly.wowyun.wowyun_device;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by user on 8/29/14.
 */
public class WowYunApp extends Application {
    private XMPPService mXMPP;
    private static final String TAG = "WowYunAppData";

    public static final int ACTIVITY_DESTROY = 0x7001;

    public static final int ANIMATION_END = 0x8001;

    public static final int XMPP_REGISTER_SUCCESS = 0x8101;
    public static final int XMPP_REGISTER_FAILURE = 0x8102;
    public static final int XMPP_DO_LOGIN = 0x8003;
    public static final int XMPP_LOGIN_SUCCESS = 0x8004;
    public static final int XMPP_NEW_BUDDY = 0x8005;
    public static final int XMPP_UPDATE_BUDDYLIST=0x8006;

    public static final int BROWSE_MEDIA_ALL = 0x9000;
    public static final int BROWSE_MEDIA_WEBALBUM = 0x9001;
    public static final int BROWSE_MEDIA_LOCAL = 0x9002;

    public static final int SNS_WEIBO_INFO_UPDATED = 0x9100;
    public static final int SNS_QZONE_INFO_UPDATED = 0x9101;

    public static final int UPDATE_UI = 0x9109;
    public String deviceID;

    private ArrayList<WebAlbumInfo> mWebAlbumImageList;
    private ArrayList<WebAlbumInfo> mWebAlbumVideoList;
    ConnectivityManager cm;
    boolean mIsConnected;

/*    Handler mHanlder = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WebScrapy.STATUS_DOLOGIN:
                    Log.i(TAG, " try to login weibo background");
                    sinaWeiBoScrapy.doLogin("吴汝旭", "xj317318");
                    break;

                case WebScrapy.STATUS_GET_WEIBO:
                    sinaWeiBoScrapy.getWeiBo();
                    break;

            }
        }
    };*/
    protected static class WebAlbumInfo {
        String thumbpath;
        String imagepath;
    }

    public WowYunApp() {
        mWebAlbumImageList = new ArrayList<WebAlbumInfo>();
        mWebAlbumVideoList = new ArrayList<WebAlbumInfo>();
    }

    public ArrayList<WebAlbumInfo> getWebAlbumInfo(boolean isImage) {
        if(isImage) {
            return mWebAlbumImageList;
        } else {
            return mWebAlbumVideoList;
        }
    }

    public XMPPService getXMPP() {
        cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        mIsConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if(mXMPP != null) {
            return mXMPP;
        }

        if(mIsConnected) {
            mXMPP=null;
            mXMPP = new XMPPService();
            try {
                do {
                    Thread.sleep(300);
                    Log.i(TAG, "Thread goto sleep, waiting for XMPP connected");
                } while (mXMPP.isConnected() == false);
            } catch (InterruptedException e) {
            }

            return mXMPP;
        } else {
            return null;
        }
    }

    public Handler getHandler() {
        return null;
    }

    public void onCreate() {
        super.onCreate();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mXMPP = new XMPPService();
                Log.i(TAG, "new XMPPService instance end, mXMPP = " + mXMPP);
            }
        };
        new Thread(runnable).start();
    }
}
