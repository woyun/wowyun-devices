package com.wowfly.wowyun.wowyun_device;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.cookie.Cookie;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Documented;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by user on 9/15/14.
 */
public class SinaWeiBoScrapy extends WebScrapy implements WebScrapy.ScrapyCallback{
    private static final String TAG = "SinaWeiBoScrapy";
    private String form_action;
    private String form_method;
    private String form_vk;
    private String username_field;
    private String password_field;
    private String weibo_uid;
    private String mWeiBoBuf;
    private SNSBrowserActivity mMainActivity;
    private Date mLastUpdate;

    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

    public SinaWeiBoScrapy(Context context, SNSBrowserActivity mainActivity) {
        loginURI = "https://login.weibo.cn/login/?ns=1&revalid=2";
        mContext = context;
        mMainActivity = mainActivity;
        mCookieStore = new WowPersistentCookieStore(mContext);
        //myCookieStore.
        mHttpClient.setUserAgent("Mozilla/5.0 (Android 4.4.4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.44");
        mHttpClient.setCookieStore(mCookieStore);
        //myCookieStore.getCookies();
        setScrapyCallback(this);
        mElemList = new ArrayList<ElementInfo>();
        //mLastUpdate = new Date();
        //mAPP = app;
    }

    private void parseLoginPageResp(byte[] resp) {
        ByteArrayInputStream is = new ByteArrayInputStream(resp);
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
                        if(parser.getName().equals("form")) {
                            form_action = parser.getAttributeValue(0);
                            form_method = parser.getAttributeValue(1);
                        } else if(parser.getName().equals("input")) {
                            Log.i(TAG, "" + parser.getAttributeValue(0) + " " + parser.getAttributeValue(1));
                            if(parser.getAttributeValue(0).equals("text")) {
                                username_field = parser.getAttributeValue(1);
                            } else if(parser.getAttributeValue(0).equals("password")) {
                                password_field = parser.getAttributeValue(1);
                            } else if(parser.getAttributeValue(0).equals("hidden")) {
                                if(parser.getAttributeValue(1).equals("vk")) {
                                    form_vk = parser.getAttributeValue(2);
                                }
                            }
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
            }

        } catch (XmlPullParserException e) {
            Log.i(TAG, " XmlPullParserException ");
        } catch (IOException e) {
            Log.i(TAG, " IOException ");
        }
    }

    private void parseWeiboPageResp(byte[] resp) {
        boolean tagStatus = false;
        ByteArrayInputStream is = new ByteArrayInputStream(resp);
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
                        Log.i(TAG, "start tag = " + parser.getName());// + " text " + parser.getAttributeValue(0));
                        if(parser.getName().equals("div")) {
                            if(parser.getAttributeValue(0).equals("ut")) {
                                tagStatus = true;
                            } else {
                                tagStatus = false;
                            }
                        } else if(tagStatus && parser.getName().equals("a")) {
                            String href = parser.getAttributeValue(0);
                            if(href.contains("/info")) {
                                Log.i(TAG, " uid info " + href);
                                break;
                            }
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
            }
        } catch (XmlPullParserException e) {
            Log.i(TAG, " XmlPullParserException " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Log.i(TAG, " IOException ");
        }
    }

    private void parseWeiBoUID(byte[] resp) {
        try {
            String str = new String(resp, "utf-8");
            int sep = str.indexOf("/info?");
            String s0 = str.substring(0, sep);
            sep = s0.lastIndexOf("href=\"/");
            weibo_uid = s0.substring(sep+6);
            Log.i(TAG, " uid " + weibo_uid);
        } catch (UnsupportedEncodingException e) {

        }

    }
    public void onFailure(int statusCode, Header[] headers, byte[] resp) {

    }
    public void onSuccess(int statusCode, Header[] headers, byte[] resp) {
        try {
            String buf = new String(resp, "utf-8");
            Log.i(TAG, " " + status + " http status " + statusCode + "\n body " + buf);
        } catch (UnsupportedEncodingException e) {
        }
        Message msg;
        switch (status) {
            case STATUS_INIT:
                parseLoginPageResp(resp);
                Log.i(TAG, " action " + form_action + " method " + form_method);
                Log.i(TAG, " username " + username_field + " password " + password_field + " vk " + form_vk);
                //doLogin();
                msg = new Message();
                msg.what = STATUS_DOLOGIN;
                msg.arg1 = SNS_TYPE_WEIBO;
                mMainActivity.mHandler.sendMessage(msg);
                break;
            case STATUS_PREPARE_LOGIN:
                break;
            case STATUS_DOLOGIN:
                //parseWeiboPageResp(resp);
                parseWeiBoUID(resp);
                msg = new Message();
                msg.what = STATUS_GET_WEIBO;
                msg.arg1 = SNS_TYPE_WEIBO;
                mMainActivity.mHandler.sendMessage(msg);
                break;
            case STATUS_GET_WEIBO:
                try {
                    if(mWeiBoBuf != null) {
                        mWeiBoBuf = null;
                    }
                    mWeiBoBuf = new String(resp, "utf-8");
                    int cnt = checkWeiboUpdate();
                    if(cnt > 0) {
                        msg = new Message();
                        msg.what = SNS_INFO_UPDATE;
                        msg.arg1 = SNS_TYPE_WEIBO;
                        msg.arg2 = cnt;
                        Bundle data = new Bundle();
                        data.putString("username", "wuruxu");
                        msg.setData(data);
                        mMainActivity.mHandler.sendMessage(msg);
                    } else {
                        mCount = getWeiBoItem(mWeiBoBuf);
                        msg = new Message();
                        msg.what = SNS_INFO_UPDATE;
                        msg.arg1 = SNS_TYPE_WEIBO;
                        mMainActivity.mHandler.sendMessage(msg);
                    }

                } catch (UnsupportedEncodingException e) {
                }
                break;
        }
    }

    public void doLogin(String username, String password) {
        RequestParams params = new RequestParams();
        params.add(username_field, username);
        params.add(password_field, password);
        params.add("remember", "on");
        params.add("backURL", "http://weibo.cn");
        params.add("backTitle", "Mobile Sina");
        params.add("vk", form_vk);
        params.add("submit", "登录");
        params.add("tryCount", "");
        mHttpClient.post("https://login.weibo.cn/login/" + form_action, params, mRespHandler);
        //mHttpClient.
        status = STATUS_DOLOGIN;
    }

    public void getProfile() {
        String SUB="", gsid_CTandWM = "";
        String _T_WM = "";

        List<Cookie> cookieList = mCookieStore.getCookies();
        for(Cookie item : cookieList) {
            Log.i(TAG, " " + item.getDomain());
            if(item.getDomain().equals(".weibo.cn")) {
                if(item.getName().equals("SUB")) {
                    SUB = item.getValue();
                } else if(item.getName().equals("gsid_CTandWM")) {
                    gsid_CTandWM = item.getValue();
                } else if(item.getName().equals("_T_WM")) {
                    _T_WM = item.getValue();
                }
            }
        }
        RequestParams params = new RequestParams();
        params.add("SUB", SUB);
        params.add("gsid_CTandWM", gsid_CTandWM);
        params.add("_T_WM", _T_WM);

        mHttpClient.get("http://weibo.cn/" + weibo_uid + "/info", params, mRespHandler);
        status = STATUS_GET_PROFILE;
    }

    public void getWeiBo() {
        String SUB="", gsid_CTandWM = "";

        List<Cookie> cookieList = mCookieStore.getCookies();
        for(Cookie item : cookieList) {
            Log.i(TAG, " " + item.getDomain());
            if(item.getDomain().equals(".weibo.cn")) {
                if(item.getName().equals("SUB")) {
                    SUB = item.getValue();
                } else if(item.getName().equals("gsid_CTandWM")) {
                    gsid_CTandWM = item.getValue();
                }
            }
        }
        RequestParams params = new RequestParams();
        params.add("SUB", SUB);
        params.add("gsid_CTandWM", gsid_CTandWM);


        mHttpClient.get("http://weibo.cn/" + weibo_uid + "/profile", params, mRespHandler);
        //mHttpClient.get
        status = STATUS_GET_WEIBO;
    }

    public int getWeiBoItem(String buf) {
        Document doc = Parser.parse(buf, "http://weibo.cn/");
        Elements elems = doc.select("div.c[id]");
        //Elements elems = doc.select("div .ctt");

        for(Element item: elems) {
            //item.select()
            //Element _item = item.removeClass("ct");
            //item.removeClass()
            item.prepend("<weibologo />");
            ElementInfo ei = new ElementInfo();
            ei.element = item;
            Elements _elems = item.getElementsByClass("ct");
            ei.date = _elems.get(0).toString().substring(17, 36);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
            try {
                Date mydate = df.parse(ei.date);
                ei.created_time = mydate.getTime()/1000;
            } catch (ParseException e) {
                //Log.i(TAG, " " + e.printStackTrace();)
                e.printStackTrace();
            }
            //Log.i(TAG, " date " + ei.date + " " + item.toString());
            mElemList.add(ei);
        }

        mCount = mElemList.size();
        return mElemList.size();
    }

    public int getWeiBoNext() {
        return 0;
    }

    public void setVC(String vc) {
        Log.i(TAG, " weibo VC isn't implement");
    }

    public int checkWeiboUpdate() {
        Date _date = new Date();
        int cnt = 0;
        Document doc = Parser.parse(mWeiBoBuf, "http://weibo.cn/");
        Elements elems = doc.select("span.ct");
        if(mLastUpdate == null) {
            Element item = elems.get(0);
            String date = item.text();
            date = date.substring(0, 19);
            Log.i(TAG, " mLastUpdate " + date);
            try {
                mLastUpdate = new Date();
                mLastUpdate = dateFormat.parse(date);
                return 0;
            } catch (ParseException e) {
            }

            return 0;
        }

        for(Element item: elems) {
            String date = item.text();
            Log.i(TAG, " item " + item.text() + " tag " + item.tagName());
            //int sep = date.indexOf("&nbsp;");
            //sep = date.indexOf(' ', sep+1);
            date = date.substring(0, 19);
            Log.i(TAG, " date = " + date);
            try {
                _date = dateFormat.parse(date);
                if(_date.getTime() > mLastUpdate.getTime()) {
                    //mLastUpdate = _date;
                    cnt ++;
                }
            } catch (ParseException e) {
                Log.i(TAG, " " + e.getMessage());
            }
        }
        return cnt;
    }
}
