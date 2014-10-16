package com.wowfly.wowyun.wowyun_device;

import android.content.Context;
import android.util.Log;
import android.widget.BaseAdapter;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by user on 9/13/14.
 */
public class AutoDownloader {
    protected ArrayList<WowYunApp.WebAlbumInfo> mWebAlbumList;
    private static final String TAG = "AutoDownloader";
    protected AsyncHttpClient mHttpClient;
    protected Context mContext;

    private class ImageDownloaderHttpHandler extends AsyncHttpResponseHandler{
        private String mFilename;
        private Context mContext;

        public ImageDownloaderHttpHandler(String filename, Context context) {
            mFilename = filename.replace('/', '-');
            mContext = context;
        }

        public void onStart() {

        }
        public void onProgress(int pos, int len) {

        }
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            FileOutputStream fos;
            try {
                fos = mContext.openFileOutput(mFilename, Context.MODE_PRIVATE);
                fos.write(responseBody);
                fos.close();

                File from = new File(mContext.getFilesDir(), mFilename);
                File to = new File(mContext.getFilesDir(), mFilename.replace('-', '_'));
                if(from.exists()) {
                    Log.i(TAG, " save media file success " + mContext.getFilesDir()+mFilename.replace('-', '_'));
                    from.renameTo(to);
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

        }
    }


    protected AsyncHttpResponseHandler mHttpHandler = new AsyncHttpResponseHandler() {
        public void onStart() {
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            Log.i(TAG, new String(responseBody));
            ByteArrayInputStream is = new ByteArrayInputStream(responseBody);
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
                            if (parser.getName().equals("item")) {
                                WowYunApp.WebAlbumInfo waItem = new WowYunApp.WebAlbumInfo();
                                String mime = parser.getAttributeValue(0);
                                waItem.thumbpath = parser.getAttributeValue(1);
                                waItem.imagepath = parser.getAttributeValue(2);
                                mWebAlbumList.add(waItem);
                                Log.i(TAG, " mime= " + mime + " thumb= " + waItem.thumbpath + " image= " + waItem.imagepath);
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
                    //displayMediaInfo();
                }
                downloadMediaFile();
            } catch (XmlPullParserException e) {
                Log.i(TAG, " XmlPullParserException ");
            } catch (IOException e) {
                Log.i(TAG, " IOException ");
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            Log.i(TAG, " statusCode = " + statusCode);
/*            TextView status = (TextView) findViewById(R.id.webalbum_status_text);
            ProgressBar bar = (ProgressBar) findViewById(R.id.webalbum_status_progressbar);

            if(statusCode == 404) {
                status.setText(getResources().getString(R.string.text_webalbum_404));
            } else {
                status.setText(getResources().getString(R.string.action_fetch_webalbum_failure));
            }
            bar.setVisibility(View.INVISIBLE);*/
        }
    };

    public void doInit(Context context) {
        mHttpClient = new AsyncHttpClient();
        mContext = context;
        mWebAlbumList = new ArrayList<WowYunApp.WebAlbumInfo>();
    }

    public void doSync(String deviceID) {
        mHttpClient.get("http://101.69.230.238:8080/list/"+deviceID.toLowerCase(), mHttpHandler);
        Log.i(TAG, " url = " + "http://101.69.230.238:8080/list/" + deviceID);
    }

    private void downloadMediaFile() {
        for(int idx=0; idx<mWebAlbumList.size(); idx++) {
            String filename = mWebAlbumList.get(idx).imagepath;
            File to = new File(mContext.getFilesDir(), filename.replace('/', '_'));
            if(to.exists() == false) {
                String url = "http://101.69.230.238:8081/" + filename;
                mHttpClient.get(url, new ImageDownloaderHttpHandler(filename, mContext));
            }
        }
    }
}
