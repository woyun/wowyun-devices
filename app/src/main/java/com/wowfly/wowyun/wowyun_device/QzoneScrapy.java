package com.wowfly.wowyun.wowyun_device;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.cookie.Cookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.MessageDigestSpi;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * Created by user on 9/18/14.
 */
public class QzoneScrapy extends WebScrapy implements WebScrapy.ScrapyCallback {
    private static final String TAG = "QzoneScrapy";
    private SNSBrowserActivity mMainActivity;
    private final static String[] hexDigits = { "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
    private byte[] uin;
    private String vcode;
    private int mGTK;

    public static String md5(byte[] inputStr) {
        return encodeByMD5(inputStr);
    }

    private static String encodeByMD5(byte[] inputStr) {
        if (inputStr != null) {
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                // 使用指定的字节数组对摘要进行最后更新，然后完成摘要计算
                byte[] results = md5.digest(inputStr);
                // 将得到的字节数组变成字符串返回
                String result = byteArrayToHexString(results);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    private String getEncryption() {
        byte[] str1;
        String str2, str3;
        str1 = hexchar2bin(MD5.md5(mPASSWORD.getBytes()));
        str2 = MD5.md5(addByte(str1, this.uin));
        str3 = MD5.md5((str2 + this.vcode).getBytes());
        return str3;

    }

    private byte[] uinToByte(String haxqq) {
        byte[] uin = new byte[8];
        String haxtext = haxqq.replaceAll("\\\\x", "");
        uin = hexchar2bin(haxtext);
        return uin;
    }

    private String getRandom(int length) {
        String text = "";// 返回的随机数
        Random random = new Random();
        for (int i = 0; i < length;) {
            int num = random.nextInt(10);
            if (i != 0 || num != 0) {
                text += num + "";
                i++;
            }
        }
        return text;

    }

    private String getTextInfo(String text, String start, String end) {
        int startIndex;
        int endIndex;
        startIndex = text.indexOf(start) + start.length();
        endIndex = text.indexOf(end, startIndex);
        text = text.substring(startIndex, endIndex);
        return text;

    }

    private byte[] hexchar2bin(String hax) {
        Log.i(TAG, " hax " + hax);
        int sep = hax.indexOf("'");
        if(sep > 0) {
            hax = hax.substring(0, sep);
        }
        Log.i(TAG, " hax " + hax);
        hax = hax.toUpperCase();// 只是看着舒服点，可以去掉
        byte[] b = new byte[hax.length() / 2];
        for (int i = 0; i < hax.length() - 1; i = i + 2) {
            b[i / 2] = (byte) Integer.parseInt(hax.substring(i, i + 2), 16);
        }
        return b;
    }

    private byte[] addByte(byte[] b1, byte[] b2) {
        byte[] by = new byte[b1.length + b2.length];
        for (int i = 0; i < b1.length; i++) {
            by[i] = b1[i];
        }
        for (int i = 0; i < b2.length; i++) {
            by[b1.length + i] = b2[i];
        }
        return by;
    }

    public void saveVC() {
        Connection.Response res;
        File file;
        byte[] b = null;// 返回值
        String url = "http://captcha.qq.com/getimage?aid=1006102&r=0."
                + getRandom(17) + "&uin=" + mUSERNAME;
/*        try {
            res = Jsoup.connect(url).cookies(cookies).ignoreContentType(true)
                    .execute();
            b = res.bodyAsBytes();
            cookies.putAll(res.cookies());
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        // 写入文件

        try {

            file = new File("/sdcard/qzone-vc.jpg");
            // 安卓的写法：
            // file=new File(this.getCacheDir(),"VC.jpg");
            FileOutputStream out = new FileOutputStream(file);
            out.write(b);
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void ptui_checkVC_prepare(String qq) {
        Connection.Response res = null;
        String url = "http://check.ptlogin2.qq.com/check?uin="
                + qq
                + "&appid=1006102&js_ver=10015&js_type=0&login_sig=y9izLTQDUx-VRJ*tu9aAnzzd3Th5R5d3-LSQ-R-DgQmZx7cRXxodffTGfDUzJtox&u1=http%3A%2F%2Fid.qq.com%2Findex.html&r="
                + getRandom(15);

        mHttpClient.get(url, mRespHandler);
        //text = connectURL(url);
    }

    private String ptui_checkVC_done(String text) {
        if(text.equals("")) {
            return text;
        }

        this.uin = uinToByte(getTextInfo(text, "\\x", "');"));// 其实是\x，但java要转义符号));
        if (getTextInfo(text, "'", "','").equals("1")) {// 需要验证码
            return ("1");
        }
        return getTextInfo(text, "','", "','");
    }

    private int getgtk(String skey) {
        int hash = 5381;


        for(char c: skey.toCharArray()) {
            hash += (hash << 5) + c;
        }
        return hash & 0x7fffffff;
    }

    private void parseQzone(String buf) {
        String jsonbuf = buf.substring(10, buf.length()-2);
        mCount = 0;
        try {
            JSONObject jsonObject = new JSONObject(jsonbuf);
            JSONArray jsonArray = jsonObject.getJSONArray("msglist");
            for(int i=0; i<jsonArray.length(); i++) {
                JSONObject obj = jsonArray.optJSONObject(i);
                if(obj != null) {
                    if(obj.getString("content").length() > 0) {
                        ElementInfo ei = new ElementInfo();
                        ei.date = obj.getString("createTime");
                        ei.created_time = obj.getLong("created_time");
                        ei.element = new Element(Tag.valueOf("span"), "");
                        ei.element.prepend("<qzonelogo />");
                        ei.element.append("<div>" + obj.getString("content") + "</div><br>");
                        //JSONArray pic = obj.getJSONArray("pic");
                        JSONArray pic = obj.optJSONArray("pic");
                        if(pic == null)
                            continue;
                        for(int j=0; j<pic.length(); j++) {
                            JSONObject img = pic.optJSONObject(j);
                            String url = img.getString("url1");
                            ei.element.append("<img src='"+url+"'  />");
                        }
                        Log.i(TAG, " qzone.content " + ei.element.toString());
                        mCount ++;
                        mElemList.add(ei);
                    }
                }
            }
            //jsonbuf = jsonbuf.replaceAll("\\/", "/");
/*            JSONArray jsonArray = new JSONArray(jsonbuf);
            for(int i=0; i<jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Log.i(TAG, " " + obj.toString());
            }*/
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public QzoneScrapy(Context context, SNSBrowserActivity mainActivity) {
        loginURI = null;//"http://ui.ptlogin2.qzone.com/cgi-bin/login?style=9&appid=549000929&pt_ttype=1&s_url=http://m.qzone.com/infocenter?g_f=";
        setScrapyCallback(this);
        mCookieStore = new WowPersistentCookieStore(context);
        //myCookieStore.
        mHttpClient.setCookieStore(mCookieStore);
        //myCookieStore.getCookies();
        //mHttpClient.setUserAgent("Mozilla/5.0 (Android 4.4.4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.44");
        //mHandler = handler;
        mHttpClient.setUserAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2062.44 Safari/537.36");
        mMainActivity = mainActivity;
        mElemList = new ArrayList<ElementInfo>();
    }

    public void onFailure(int statusCode, Header[] headers, byte[] resp) {
        if(actionMode == ACTION_BIND) {
/*            Message msg = new Message();http://t1.qpic.cn/mblogpic/0ac8bd706914abef3eb2/460
            msg.what = SNSSyncActivity.BIND_FAILURE;
            mHandler.sendMessage(msg);*/
        }
    }

    public void onSuccess(int statusCode, Header[] headers, byte[] resp) {
        try {
            String str = new String(resp, "utf-8");
            Log.i(TAG, " " + status + " http status " + str);

            switch (status) {
                case STATUS_INIT:
                    vcode = ptui_checkVC_done(str);
                    if(vcode == "1") {
                        Log.i(TAG, " ******* vcode needed ******* ");
                        //saveVC();
                        getQQVC();
                    } else {
                        status = STATUS_DOLOGIN;
                        Message msg = new Message();
                        msg.what = STATUS_DOLOGIN;
                        msg.arg1 = SNS_TYPE_QZONE;
                        mMainActivity.mHandler.sendMessage(msg);
                    }
                    break;

                case STATUS_DOLOGIN:
                    Message msg = new Message();
                    msg.what = STATUS_GET_WEIBO;
                    msg.arg1 = SNS_TYPE_QZONE;
                    mMainActivity.mHandler.sendMessage(msg);
                    break;

                case STATUS_GET_VC:
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("qzonevc", resp);
                    msg = new Message();
                    msg.what = STATUS_GET_VC;
                    msg.arg1 = SNS_TYPE_QZONE;
                    msg.setData(bundle);
                    mMainActivity.mHandler.sendMessage(msg);
/*                    try {

                        File file = new File("/sdcard/qzone-vc.jpg");
                        // 安卓的写法：
                        // file=new File(this.getCacheDir(),"VC.jpg");
                        FileOutputStream out = new FileOutputStream(file);
                        out.write(resp);
                        out.close();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }*/
                    break;

                case STATUS_GET_WEIBO:
                    //String jsonbuf = str.substring(10, str.length()-1);
                    parseQzone(str);
                    msg = new Message();
                    msg.what = SNS_INFO_UPDATE;
                    msg.arg1 = SNS_TYPE_QZONE;
                    mMainActivity.mHandler.sendMessage(msg);
                    break;
            }
        } catch (UnsupportedEncodingException e) {
        }
    }

    public void doPostInit(String qq) {
        if(vcode == null) {
            ptui_checkVC_prepare(qq);
        } else {
            status = STATUS_DOLOGIN;
            Message msg = new Message();
            msg.what = STATUS_DOLOGIN;
            msg.arg1 = SNS_TYPE_QZONE;
            mMainActivity.mHandler.sendMessage(msg);
        }
    }

    public void doBind() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                QzoneProto qzoneProto = new QzoneProto(mUSERNAME, mPASSWORD);
                String info = qzoneProto.login();
                int sep = info.indexOf("登录成功");
                //Log.i(TAG, " sep " + sep + " sub "+ info.substring(sep));
/*                if(sep > 0) {
                    String nickname = info.substring(sep+8);
                    //Log.i(TAG, " nickname.01 " + nickname);
                    int s0 = nickname.indexOf("'");
                    int s1 = nickname.lastIndexOf("'");
                    mNICKNAME = nickname.substring(s0+1, s1);
                    //Log.i(TAG, " nickname.02 " + mNICKNAME + " s0 " + s0 + " s1 " + s1);
                    Message msg = new Message();
                    msg.what = SNSSyncActivity.BIND_SUCCESS;
                    Bundle data = new Bundle();
                    data.putString("username", mUSERNAME);
                    data.putString("password", mPASSWORD);
                    data.putString("nickname", mNICKNAME);
                    data.putString("snstype", "qzone");
                    Log.i(TAG, " send qzone bind message " + mNICKNAME + " " + mUSERNAME);
                    msg.setData(data);
                    mHandler.sendMessage(msg);
                }*/

                Log.i(TAG, " qzone login " + info);
/*                info = qzoneProto.getQzoneProfile();
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter("/sdcard/qzone-01.txt"));
                    writer.write(info);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                //Log.i(TAG, " qzone getprofile " + info);
            }
        };
        new Thread(runnable).start();
    }

    public void doLogin(String username, String password) {
        Log.i(TAG, " login " + username + " " + password);
        mUSERNAME = username;
        mPASSWORD = password;
        String url = "http://ptlogin2.qq.com/login?u=";
        url = url + mUSERNAME + "&p="+getEncryption()+"&verifycode="+vcode;
        url = url + "&aid=1006102&u1=http%3A%2F%2Fid.qq.com%2Findex.html%23myfriends&h=1&ptredirect=1&ptlang=2052&from_ui=1&dumy=&fp=loginerroralert&action=8-57-411578&mibao_css=&t=5&g=1&js_type=0&js_ver=10015&login_sig=M68RroVE7d9cWVGLMysPechIltwu1GWLDkOrMwJ1O2VISYLTKwX6t3*qLIwl1DIa";

        mHttpClient.get(url, mRespHandler);
    }
    public void getQQVC() {
        String url = "http://captcha.qq.com/getimage?aid=1006102&r=0." + getRandom(17) + "&uin=" + mUSERNAME;
        mHttpClient.get(url, mRespHandler);
        status = STATUS_GET_VC;
    }
    public void getShuoShuo() {
        List<Cookie> cookieList = mCookieStore.getCookies();
        for(Cookie item : cookieList) {
            if(item.getDomain().equals("qq.com")) {
                Log.i(TAG, " " + item.getDomain() + " " + item.getName() + " " + item.getValue());
                if(item.getName().equals("skey")) {
                    mGTK = getgtk(item.getValue());
                }
            }
        }
        String url = "http://taotao.qq.com/cgi-bin/emotion_cgi_msglist_v6?uin="+mUSERNAME + "&ftype=0&sort=0&pos=0&num=20&replynum=100&g_tk="+mGTK+"&format=json&need_private_comment=1";
        mHttpClient.get(url, mRespHandler);
        status = STATUS_GET_WEIBO;
    }

    public void setVC(String vc) {
        Log.i(TAG, " QZONE set vc " + vc);
        vcode = vc.toUpperCase();
        doPostInit("");
        //mCookieStore.
        mCookieStore.clearExpired(Calendar.getInstance().getTime());
    }
}
