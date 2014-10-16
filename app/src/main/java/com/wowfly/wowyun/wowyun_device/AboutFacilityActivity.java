package com.wowfly.wowyun.wowyun_device;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.TextView;

/**
 * Created by user on 9/19/14.
 */
public class AboutFacilityActivity extends Activity {
    private WebView webView;
    private WifiManager mWiFi;

    public void onCreate(Bundle saved) {
        super.onCreate(saved);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_about_device);
        //webView = (WebView) findViewById(R.id.webview);

        mWiFi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wi = mWiFi.getConnectionInfo();
        TextView tv = (TextView) findViewById(R.id.macaddr);
        tv.setText(wi.getMacAddress().toUpperCase());
        tv = (TextView) findViewById(R.id.ipaddr);
        tv.setText(intToIp(wi.getIpAddress()));
        //webView.loadUrl("http://101.69.230.238:8081/about/about_company.html");
    }

    private String intToIp(int i) {
        return (( i & 0xFF) + "." + ((i >> 8 ) & 0xFF) + "." + ((i >> 16 ) & 0xFF) + "." + ((i >> 24 ) & 0xFF));
/*        return ((i >> 24 ) & 0xFF ) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ( i & 0xFF) ;*/
    }
}
