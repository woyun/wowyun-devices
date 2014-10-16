package com.wowfly.wowyun.wowyun_device;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.nodes.Element;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import android.os.AsyncTask;

/**
 * Created by user on 9/15/14.
 */
public class SNSBrowserActivity extends Activity implements WebScrapy.ScrapyCallback{
    private static final String TAG = "SNSBrowserActivity";
    SNSHeaderView headerView;
    private ListView mSNSList;
    private View mLoadingTips;
    private SharedPreferences mPref;
    //private ArrayList<SNSPerson> mSNSPersonList;
    private HashMap<String, SNSPerson> mSNSPersonList;
    public Handler mHandler;
    private String username;
    private int mSortType;

    public void onSuccess(int statusCode, Header[] headers, byte[] resp) {

    }

    public void onFailure(int statusCode, Header[] headers, byte[] resp) {

    }

    private void initPref(SNSHeaderView view) {
        Map<String, ?> keys = mPref.getAll();
        mSNSPersonList = new HashMap<String, SNSPerson>();

        for(Map.Entry<String, ?> entry: keys.entrySet()) {
            String key = entry.getKey();
            key = key.replace(".", "_");
            String[] keyitem = key.split("_");
            Log.i(TAG, " key " + entry.getKey() + " val " + entry.getValue().toString() + " len " + keyitem.length + " keyitem " + keyitem);
            if(keyitem.length < 4 ) continue;
            if(keyitem[0].equals("sns") && keyitem[3].equals("username")) {
                Log.i(TAG, " keyitem[1] " + keyitem[1]);
                SNSPerson p = (SNSPerson) mSNSPersonList.get(keyitem[1]);
                if(p == null) {
                    Log.i(TAG, " new SNSPerson " + keyitem[0]);
                    String password = mPref.getString(keyitem[0]+"."+keyitem[1]+"."+keyitem[2]+".password", "");
                    if(entry.getValue().toString().length() > 0) {
                        p = new SNSPerson(entry.getValue().toString(), password, BitmapFactory.decodeResource(getResources(), R.drawable.default_person_icon), keyitem[2], getApplicationContext(), this);
                        mSNSPersonList.put(keyitem[1], p);
                        view.addSNSPerson(p);
                    }
                } else {
                    String password = mPref.getString(keyitem[0]+"."+keyitem[1]+"."+keyitem[2]+".password", "");
                    if(entry.getValue().toString().length() > 0)
                        p.addSNSAccount(entry.getValue().toString(), password, keyitem[2]);
                }
            }
        }

        Iterator iter = mSNSPersonList.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            Log.i(TAG, " login person sns " + key.toString());
            SNSPerson p = (SNSPerson) val;
            p.doInit();
        }
    }

    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        //mSNSPersonList = new ArrayList<SNSPerson>();
        setContentView(R.layout.activity_sns_browser);
        headerView = (SNSHeaderView) findViewById(R.id.snsheaderview);
        //SNSPerson p = new SNSPerson("Tom", BitmapFactory.decodeResource(getResources(), R.drawable.default_person_icon));
        //headerView.addSNSPerson(p);
        //p = new SNSPerson("Jerry", BitmapFactory.decodeResource(getResources(), R.drawable.default_person_icon));
        //headerView.addSNSPerson(p);

        mPref = getSharedPreferences("wowyun-device", Context.MODE_PRIVATE);

        TextView textView = (TextView) findViewById(R.id.snsloading_info);
        textView.setText(R.string.snsloadinginfo);

        mLoadingTips = findViewById(R.id.snsloading);

        mSNSList = (ListView) findViewById(R.id.snsinfolist);
        mSNSList.setAdapter(new SNSItemAdapter());
        initPref(headerView);
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch(msg.what) {
                    case WebScrapy.STATUS_DOLOGIN:
                        SNSPerson p = headerView.getCurrentSNSPerson();
                        if(msg.arg1 == WebScrapy.SNS_TYPE_WEIBO) {
                            p.doLoginBySNS("weibo");
                        } else if(msg.arg1 == WebScrapy.SNS_TYPE_QZONE) {
                            Log.i(TAG, " try to login qzone");
                            p.doLoginBySNS("qzone");
                        }
                        break;

                    case WebScrapy.STATUS_GET_WEIBO:
                        if(msg.arg1 == WebScrapy.SNS_TYPE_WEIBO) {
                            p = headerView.getCurrentSNSPerson();
                            p.getSNSInfo("weibo");
                        } else if(msg.arg1 == WebScrapy.SNS_TYPE_QZONE) {
                            p = headerView.getCurrentSNSPerson();
                            p.getSNSInfo("qzone");
                        }
                        break;

                    case WebScrapy.STATUS_GET_VC:
                        p = headerView.getCurrentSNSPerson();
                        Bundle bundle = msg.getData();
                        Bitmap bm;

                        ByteArrayInputStream is = new ByteArrayInputStream(bundle.getByteArray("qzonevc"));
                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inPreferredConfig = Bitmap.Config.RGB_565;
                        opt.inDither = true;
                        Rect _rt = new Rect();
                        try {
                            bm = BitmapFactory.decodeStream(is, _rt, opt);
                            showVCDialog(bm, msg.arg1, p);
                            //mImageView.setImageBitmap(bm);
                        } catch (OutOfMemoryError e0) {
                            e0.printStackTrace();
                        }
                        //opt.inSampleSize = 2;

                        Log.i(TAG, " display VC dialog here");
                        break;

                    case WebScrapy.SNS_INFO_UPDATE:
                        if(msg.arg1 == WebScrapy.SNS_TYPE_WEIBO) {
                            Log.i(TAG, " weibo sns updated.");
                        }
                        mLoadingTips.setVisibility(View.GONE);
                        mSNSList.setVisibility(View.VISIBLE);
                        BaseAdapter adapter = (BaseAdapter) mSNSList.getAdapter();
                        adapter.notifyDataSetChanged();
                        mSNSList.requestFocusFromTouch();
                        break;
                }
            }
        };
    }

    private void showVCDialog(Bitmap bm, final int snstype, final SNSPerson person) {
        LayoutInflater _inflater = getLayoutInflater();
        View _layout = _inflater.inflate(R.layout.dialog_vc_input, null);
        final EditText _input = (EditText) _layout.findViewById(R.id.dialog_vc_input_edit);
        ImageView imageView = (ImageView) _layout.findViewById(R.id.dialog_vc_bitmap);
        imageView.setImageBitmap(bm);

        final AlertDialog.Builder pwdBuilder = new AlertDialog.Builder(SNSBrowserActivity.this);
        pwdBuilder.setTitle("验证码输入")
                .setView(_layout)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Editable text = _input.getText();
                        Log.i(TAG, " vcode " + text.toString());
                        person.setVC(snstype, text.toString());
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();

    }

    protected void showBrowseTypeSelectionDialog(Activity activity, int title_sid,  int opt_sid) {
        final AlertDialog dlg;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(getResources().getString(title_sid));
        //builder.setSingleChoiceItems()
        int val = mPref.getInt("option.sns.sorttype", 0);
        builder.setSingleChoiceItems(opt_sid, val, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Log.i(TAG, " select item = " + i);
                switch (i) {
                    case 0:
                        mSortType = SNSPerson.SNS_SORT_BY_PERSON;
                        break;
                    case 1:
                        mSortType = SNSPerson.SNS_SORT_BY_DATE;
                        break;
                    case 2:
                        mSortType = SNSPerson.SNS_SORT_BY_SOURCE;
                        break;
                }
                //mInfoHeader.setText(String.format(mFormatString, mType == MEDIA_TYPE_IMAGE ? getImageCount() : getVideoCount()));
                headerView.getCurrentSNSPerson().setSNSSort(mSortType);
                BaseAdapter adapter = (BaseAdapter) mSNSList.getAdapter();
                adapter.notifyDataSetChanged();

                SharedPreferences.Editor editor = mPref.edit();
                editor.putInt("option.sns.sorttype", mSortType-SNSPerson.SNS_SORT_BY_PERSON);
                editor.commit();

                dialogInterface.cancel();
            }
        });
        dlg = builder.create();
        dlg.show();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            headerView.gotoNextIcon();
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            headerView.gotoPrevIcon();
            return true;
        } else if(keyCode == KeyEvent.KEYCODE_MENU) {
            showBrowseTypeSelectionDialog(this, R.string.snsbrowsetype, R.array.snssortopt);
        }

        return super.onKeyDown(keyCode, event);
    }

    public class URLDrawable extends BitmapDrawable {
        protected Drawable drawable;

        public URLDrawable() {
            setBounds(0, 0, 220, 100);
        }

        @Override
        public void draw(Canvas canvas) {
            if(drawable != null) {
                drawable.draw(canvas);
            }
        }
    }

    private class URLImageParser implements Html.ImageGetter {
        TextView c;
        TextView container = null;
        URLDrawable urlDrawable;
        ImageView snslogo;

        public URLImageParser(TextView c, ImageView snslogo) {
            this.c = c;
            this.snslogo = snslogo;
            container = c;
        }

        public Drawable getDrawable(String source) {
            urlDrawable = new URLDrawable();
            Log.i(TAG, " get HTTP img.source " + source);
            if(source.startsWith("http")) {
                if(source.contains("u1.sinaimg.cn/upload") && source.contains(".gif")) {
                    urlDrawable.setBounds(0, 0, 11, 11);
                } else if(source.contains(".sinaimg.cn/wap") && source.contains(".jpg")) {
                    urlDrawable.setBounds(0, 0, 180, 100);
                }

                ImageGetterAsyncTask asyncTask = new ImageGetterAsyncTask(urlDrawable);
                asyncTask.execute(source);
            } else {
/*                if(source.contains("weibo")) {
                    Drawable d = getResources().getDrawable(R.drawable.ic_logo_weibo_24x24);
                    d.setBounds(0, 0, 24, 24);
                    this.snslogo.setImageDrawable(d);
                    return null;
                } else if(source.contains("qzone")) {
                    Drawable d = getResources().getDrawable(R.drawable.ic_logo_qzone_24x24);
                    d.setBounds(0, 0, 24, 24);
                    this.snslogo.setImageDrawable(d);
                    return null;
                }*/
            }
            return urlDrawable;
        }

        public class ImageGetterAsyncTask extends AsyncTask<String, Void, Drawable>  {
            URLDrawable urlDrawable;

            public ImageGetterAsyncTask(URLDrawable d) {
                this.urlDrawable = d;
            }

            @Override
            protected Drawable doInBackground(String... params) {
                String source = params[0];
                return fetchDrawable(source);
            }

            @Override
            protected void onPostExecute(Drawable result) {
                float multiplier = (float)200 / (float)result.getIntrinsicWidth();
                int width = (int)(result.getIntrinsicWidth() * multiplier);
                int height = (int)(result.getIntrinsicHeight() * multiplier);
                //c.getHeight();
                //c.getMeasuredHeight();
                width = result.getIntrinsicWidth();
                height = result.getIntrinsicHeight();
                //Log.i(TAG, " width " + width + " height " + height + " c.getHeight " + c.getMeasuredHeight());
                urlDrawable.setBounds(0, 0, width, height);
                urlDrawable.drawable = result;
                //urlDrawable.setGravity(Gravity.FILL);
                //URLImageParser.this.container.setHeight(120);
                URLImageParser.this.container.invalidate();
                //URLImageParser.this.container.setH
            }

            public Drawable fetchDrawable(String urlString) {
                try {
                    //Log.i(TAG, " fetch img " + urlString);
                    InputStream is = fetch(urlString);
                    Drawable drawable = Drawable.createFromStream(is, "src");
                    //drawable.getBounds();
                    //drawable.get
                    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                    return drawable;
                } catch (Exception e) {
                    return null;
                }
            }

            private InputStream fetch(String urlString) throws MalformedURLException, IOException {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet request = new HttpGet(urlString);
                HttpResponse response = httpClient.execute(request);
                return response.getEntity().getContent();
            }
        }
    }
    private class SNSItemAdapter extends BaseAdapter {
        private ArrayList<Element> elements;

        public int getCount() {
            SNSPerson p = headerView.getCurrentSNSPerson();
            if(p != null) {
                //Log.i(TAG, " getCount " + p.getSNSItemCount());
                elements = p.getSNSItemList();
                return p.getSNSItemCount();
            } else {
                return 0;
            }
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int pos, View view, ViewGroup parent) {
            View myview = view;
            Element elem;

            if(myview == null) {
                myview = SNSBrowserActivity.this.getLayoutInflater().inflate(R.layout.item_list_sns, parent, false);
                //SNSPerson p = headerView.getCurrentSNSPerson();
                //ArrayList<Element> elements = p.getSNSItemList();
                elem = elements.get(pos);
                //elem.prepend("<img src='weibo_logo' />");
                //myview.setTag(holder);
            } else {
                //holder = (ViewHolder) view.getTag();
                //SNSPerson p = headerView.getCurrentSNSPerson();
                //ArrayList<Element> elements = p.getSNSItemList();
                elem = elements.get(pos);
            }
            TextView textView = (TextView) myview.findViewById(R.id.sns_text);
            ImageView snslogo = (ImageView) myview.findViewById(R.id.sns_logo);
            //TextView mediaView = (TextView) myview.findViewById(R.id.sns_media);

            //elem.append("<img src='http://www.sinaimg.cn/blog/developer/wiki/LOGO_16x16.png' />");
            //Log.i(TAG, " elem " + elem.toString());
            //Html.fromHtml()
            URLImageParser urlImageParser = new URLImageParser(textView, snslogo);
            //Html.
            Spanned spanned = Html.fromHtml(elem.toString(), urlImageParser, new SNSTagHandler(myview));
            //spanned.
            //spanned.
            textView.setMovementMethod(new ScrollingMovementMethod());
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setClickable(true);
            textView.setGravity(Gravity.FILL);
            //textView.setAllCaps(true);
            //textView.
            textView.setText(spanned);
            //mediaView.setText(Html.fromHtml(elem.toString(), urlImageParser, null));
            return myview;
        }
    }
}
