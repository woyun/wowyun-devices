package com.wowfly.wowyun.wowyun_device;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by user on 9/19/14.
 */
public class AboutCompanyActivity extends Activity {
    private WebView webView;
    private static final String TAG = "AboutCompanyActivity";

    public void onCreate(Bundle saved) {
        super.onCreate(saved);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_about_company);
        webView = (WebView) findViewById(R.id.webview);

        webView.loadUrl("http://101.69.230.238:8081/about/about_company.html");
        webView.getSettings().setDefaultFontSize(16);

        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                Log.i(TAG, " onPageFinished " + url);
            }
        });
        //webView.
    }
}
