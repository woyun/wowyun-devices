package com.wowfly.wowyun.wowyun_device;

import android.app.Application;
import android.util.Log;

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

    public String deviceID;

    public XMPPService getXMPP() {
        return mXMPP;
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
