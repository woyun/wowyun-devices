package com.wowfly.wowyun.wowyun_device;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;

/**
 * Created by user on 9/7/14.
 */
public class ToolsActivity extends Activity {
    private ListView mToolList;

    public class ToolItemInfo {
        int iconRes;
        String name;
        String activityname;
    }

    private ArrayList<ToolItemInfo> mItemsList;

    public ToolsActivity() {
        mItemsList = new ArrayList<ToolItemInfo>();
        ToolItemInfo ti = new ToolItemInfo();
        ti.iconRes = R.drawable.tool_data;
        ti.name = "万年历";
        ti.activityname = "cn.etouch.ecalendar.pad";
        mItemsList.add(ti);

        ti = new ToolItemInfo();
        ti.iconRes = R.drawable.tool_radio;
        ti.name = "网络收音机";
        ti.activityname = "cn.etouch.ecalendar.pad";
        mItemsList.add(ti);

        ti = new ToolItemInfo();
        ti.iconRes = R.drawable.tool_alarm;
        ti.name = "小闹钟";
        ti.activityname = "cn.etouch.ecalendar.pad";
        mItemsList.add(ti);

        ti = new ToolItemInfo();
        ti.iconRes = R.drawable.tool_light;
        ti.name = "小夜灯";
        ti.activityname = "cn.etouch.ecalendar.pad";
        mItemsList.add(ti);
    }

    public void onCreate(Bundle saved) {
        super.onCreate(saved);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_tools);
        mToolList = (ListView) findViewById(R.id.tools_list);
        mToolList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ToolItemInfo ti = mItemsList.get(i);
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(ti.activityname, ti.activityname+".LoadingActivity"));
                startActivity(intent);
            }
        });
        mToolList.setAdapter(new ToolsItemAdapter());
    }

    private class ToolsItemAdapter extends BaseAdapter {
        public int getCount() {
            return mItemsList.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int pos, View view, ViewGroup parent) {
            View myview = view;

            if(myview == null) {
                myview = ToolsActivity.this.getLayoutInflater().inflate(R.layout.item_list_toolitem, parent, false);

                ImageView icon = (ImageView) myview.findViewById(R.id.tool_icon);
                TextView name = (TextView) myview.findViewById(R.id.tool_name);

                icon.setImageResource(mItemsList.get(pos).iconRes);
                name.setText(mItemsList.get(pos).name);
                //myview.setTag(holder);
            } else {
                //holder = (ViewHolder) view.getTag();
            }

            return myview;
        }
    }
}
