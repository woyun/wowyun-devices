package com.wowfly.wowyun.wowyun_device;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by user on 9/15/14.
 */
public class SNSPerson {
    private static final String TAG = "SNSPerson";
    //private String mUserName;
    private Bitmap mIcon;
    private String mName;
    private Context mContext;
    private SNSBrowserActivity mSNSActivity;
    private int mCount;
    //private String mSNSType;
    //private String mPassword;

    final static int SNS_SORT_BY_PERSON = 0x1000;
    final static int SNS_SORT_BY_DATE = 0x1001;
    final static int SNS_SORT_BY_SOURCE = 0x1002;

    private int mSort;
    private class SNSAccountInfo {
        public String username;
        public String password;
        public WebScrapy webScrapy;
    }

    private HashMap<String, SNSAccountInfo> mAccountList;

    public SNSPerson() {
    }

    public SNSPerson(String username, String password, Bitmap icon, String snstype, Context context, SNSBrowserActivity mainActivity) {
        SNSAccountInfo accountInfo = new SNSAccountInfo();
        accountInfo.username = username;
        accountInfo.password = password;
        if(snstype.equals("weibo")) {
            accountInfo.webScrapy = (WebScrapy) new SinaWeiBoScrapy(context, mainActivity);
        } else if(snstype.equals("qzone")) {
            accountInfo.webScrapy = (WebScrapy) new QzoneScrapy(context, mainActivity);
        }
        mIcon = icon;
        mContext = context;
        mSNSActivity = mainActivity;
        mAccountList = new HashMap<String, SNSAccountInfo>();
        mAccountList.put(snstype, accountInfo);
        mSort = SNS_SORT_BY_DATE;

    }

    public void addSNSAccount(String username, String password, String snstype) {
        SNSAccountInfo accountInfo = new SNSAccountInfo();
        accountInfo.username = username;
        accountInfo.password = password;
        if(snstype.equals("weibo")) {
            accountInfo.webScrapy = (WebScrapy) new SinaWeiBoScrapy(mContext, mSNSActivity);
        } else if(snstype.equals("qzone")) {
            accountInfo.webScrapy = (WebScrapy) new QzoneScrapy(mContext, mSNSActivity);
        }
        mAccountList.put(snstype, accountInfo);
    }

    public String getPassword(String snstype) {
        SNSAccountInfo accountInfo = mAccountList.get(snstype);
        return accountInfo.password;
    }

    public String getUsername(String snstype) {
        SNSAccountInfo accountInfo = mAccountList.get(snstype);
        return accountInfo.username;
    }

    public void doLoginBySNS(String snstype) {
        SNSAccountInfo accountInfo = mAccountList.get(snstype);
        accountInfo.webScrapy.doLogin(accountInfo.username, accountInfo.password);
    }

    public void doLogin() {
        Iterator iter = mAccountList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            Log.i(TAG, " sns " + key.toString() + " doLogin ");
            //SNSAccountInfo accountInfo = (SNSAccountInfo) val;
            doLoginBySNS(key.toString());
        }
    }

    public void setVC(int snstype, String vc) {
        if(snstype == WebScrapy.SNS_TYPE_QZONE) {
            SNSAccountInfo accountInfo = mAccountList.get("qzone");
            accountInfo.webScrapy.setVC(vc);
        }
    }
    public void doInit() {
        Iterator iter = mAccountList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            SNSAccountInfo accountInfo = (SNSAccountInfo) val;
            Log.i(TAG, " sns " + key.toString() + " doLogin " + accountInfo.webScrapy);
            accountInfo.webScrapy.doInit();
            if(key.toString().equals("qzone")) {
                QzoneScrapy qzoneScrapy = (QzoneScrapy) accountInfo.webScrapy;
                qzoneScrapy.doPostInit(accountInfo.username);
            }
            //doLoginBySNS(key.toString());
        }
    }

    public void getSNSInfo(String snstype) {
        SNSAccountInfo accountInfo = mAccountList.get(snstype);
        if(snstype.equals("weibo")) {
            SinaWeiBoScrapy sinaWeiBoScrapy = (SinaWeiBoScrapy) accountInfo.webScrapy;
            sinaWeiBoScrapy.getWeiBo();
        } else if(snstype.equals("qzone")) {
            QzoneScrapy qzoneScrapy = (QzoneScrapy) accountInfo.webScrapy;
            qzoneScrapy.getShuoShuo();
        }
    }

    public int getSNSItemCount() {
        mCount = 0;
        Iterator iter = mAccountList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            SNSAccountInfo accountInfo = (SNSAccountInfo) val;
            //Log.i(TAG, " sns " + key.toString() + " doLogin " + accountInfo.webScrapy);
            //if(key.toString().equals("weibo") == false)
            //    continue;
            //SinaWeiBoScrapy sinaWeiBoScrapy = (SinaWeiBoScrapy) accountInfo.webScrapy;
            //mCount += sinaWeiBoScrapy.getWeiboCount();
            mCount += accountInfo.webScrapy.getElemCount();
            //accountInfo.webScrapy.doInit();
            //doLoginBySNS(key.toString());
        }

        return mCount;
    }

    public void setSNSSort(int sorttype) {
        mSort = sorttype;
    }

    public ArrayList<Element> getSNSItemList() {
        ArrayList<Element> elements = new ArrayList<Element>();
        ArrayList<WebScrapy.ElementInfo> elementInfoArrayList = new ArrayList<WebScrapy.ElementInfo>();

        Iterator iter = mAccountList.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            SNSAccountInfo accountInfo = (SNSAccountInfo) val;
            //Log.i(TAG, " sns " + key.toString() + " doLogin " + accountInfo.webScrapy);
            //if(key.toString().equals("weibo") == false)
            //    continue;
            //SinaWeiBoScrapy sinaWeiBoScrapy = (SinaWeiBoScrapy) accountInfo.webScrapy;
            //mCount += sinaWeiBoScrapy.getWeiboCount();
            //mCount += accountInfo.webScrapy.getElemCount();
/*            for(Element element: accountInfo.webScrapy.getElemList()) {
                element = element.prepend("<img src='weibo_logo' />");
                elements.add(element);
            }*/

            //elements.addAll(accountInfo.webScrapy.getElemList());
            elementInfoArrayList.addAll(accountInfo.webScrapy.getElemList());
            //accountInfo.webScrapy.doInit();
            //doLoginBySNS(key.toString());
        }

        if(mSort == SNS_SORT_BY_DATE) {
            Collections.sort(elementInfoArrayList, new Comparator<WebScrapy.ElementInfo>() {
                @Override
                public int compare(WebScrapy.ElementInfo elementInfo, WebScrapy.ElementInfo elementInfo2) {
                    return elementInfo.created_time > elementInfo2.created_time ? 1 : 0;
                }
            });
        } else if(mSort == SNS_SORT_BY_PERSON) {
            Collections.sort(elementInfoArrayList, new Comparator<WebScrapy.ElementInfo>() {
                @Override
                public int compare(WebScrapy.ElementInfo elementInfo, WebScrapy.ElementInfo elementInfo2) {
                    return elementInfo.username.compareTo(elementInfo2.username);
                }
            });
        }
        for(WebScrapy.ElementInfo ei : elementInfoArrayList) {
            elements.add(ei.element);
        }
/*        for(WebScrapy.ElementInfo ei: accountInfo.webScrapy.getElemList()) {
            elements.add(ei.element);
            Log.i(TAG, " date " + ei.date + " created_time " + ei.created_time);//+ " elem " + ei.element);
        }*/

        return elements;
    }

    public SNSAccountInfo getAccountInfo(String snstype) {
        return mAccountList.get(snstype);
    }

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public Bitmap getIcon() {
        return mIcon;
    }
}
