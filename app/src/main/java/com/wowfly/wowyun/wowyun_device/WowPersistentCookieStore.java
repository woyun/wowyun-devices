package com.wowfly.wowyun.wowyun_device;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.PersistentCookieStore;

import org.apache.http.cookie.Cookie;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 9/15/14.
 */
public class WowPersistentCookieStore extends PersistentCookieStore {
    private static final String TAG = "WowPersistentCookieStore";
    private ArrayList<Cookie> cookieList;

    public WowPersistentCookieStore(Context context) {
        super(context);
        //cookieList = new ArrayList<Cookie>();
    }

    public void addCookie(Cookie cookie) {
        if(cookie.getValue().length() > 0) {
            super.addCookie(cookie);
        }
        Log.i(TAG, " addCookie " + cookie.toString() + " length " + cookie.getValue().length());

        //cookie.
        //cookie.
        //super.addCookie(cookie);
        //cookieList.add(cookie);
    }

    public List<Cookie> getCookies() {
        return super.getCookies();
        //Log.i(TAG, " cookieList.size " + cookieList.size());
        //return cookieList;
    }
}
