package com.wowfly.wowyun.wowyun_device;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by user on 9/7/14.
 */
public class BuddyListActivity extends Activity {
    private static final String TAG = "BuddyListActivity";

    private ListView mBuddyList;
    private XMPPService mXMPP;
    private WowYunApp mAPP;
    private Handler mHandler;

    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_buddylist);

        mBuddyList = (ListView) findViewById(R.id.buddy_list);
        mBuddyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            }
        });
        mBuddyList.setAdapter(new BuddyItemAdapter());

        //mAPP = (WowYunApp) getApplication();

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case WowYunApp.XMPP_UPDATE_BUDDYLIST:
                        Log.i(TAG, " update buddy list");
                        BaseAdapter adapter = (BaseAdapter)mBuddyList.getAdapter();
                        adapter.notifyDataSetChanged();
                        break;
                }
            }
        };

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mAPP = (WowYunApp)getApplication();
                do {
                    mXMPP = mAPP.getXMPP();
                    if(mXMPP == null) {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            Log.i(TAG, "sleep Exception " + e.getMessage());
                            continue;
                        }
                        Log.i(TAG, "Thread goto sleep, for XMPP service initialize");
                    }
                } while(mXMPP == null);
                //mXMPP.setMainActivity(MainActivity.this);
                //mXMPP.addRosterListener(MainActivity.this.mRosterListener);
                mXMPP.getBuddyList();
                Message msg = new Message();
                msg.what = WowYunApp.XMPP_UPDATE_BUDDYLIST;
                mHandler.sendMessage(msg);
                Log.i(TAG, " XMPP = " + mXMPP + " count " + mXMPP.getBuddyCount());
                //BaseAdapter adapter = (BaseAdapter)mBuddyList.getAdapter();
                //mBuddyList.setAdapter(adapter);
                //adapter.notifyDataSetInvalidated();
            }
        };
        new Thread(runnable).start();
    }

    private class BuddyItemAdapter extends BaseAdapter {
        public int getCount() {
            if(mXMPP == null) {
                return 0;
            } else {
                return mXMPP.getBuddyCount();
            }
        }

        public Object getItem(int position) {
            return mXMPP.getBuddyInfo(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int pos, View view, ViewGroup parent) {
            View myview = view;

            if(myview == null) {
                myview = BuddyListActivity.this.getLayoutInflater().inflate(R.layout.item_list_buddyitem, parent, false);
                //myview.setTag(holder);
            } else {
                //holder = (ViewHolder) view.getTag();
            }

            ImageView icon = (ImageView) myview.findViewById(R.id.buddy_icon);
            TextView name = (TextView) myview.findViewById(R.id.buddy_name);
            XMPPService.BuddyInfo bi = mXMPP.getBuddyInfo(pos);
            if(bi.isAvailable)
                icon.setImageResource(R.drawable.ic_buddy_online);
            else
                icon.setImageResource(R.drawable.ic_buddy_offline);
            //icon.setImageResource(mItemsList.get(pos).iconRes);
            name.setText(bi.name);

            return myview;
        }
    }
}
