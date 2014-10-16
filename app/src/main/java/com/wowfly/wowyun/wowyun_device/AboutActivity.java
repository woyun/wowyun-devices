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
 * Created by user on 9/18/14.
 */
public class AboutActivity extends Activity {
    private ListView mAboutList;
    private static final String TAG = "AboutActivity";

    public class AboutItemInfo {
        int iconRes;
        int iconResHi;
        String name;
        String activityname;
        Class<?> cls;
    }

    private ArrayList<AboutItemInfo> mItemsList;

    public AboutActivity() {
        mItemsList = new ArrayList<AboutItemInfo>();
        AboutItemInfo ti = new AboutItemInfo();
        ti.iconRes = R.drawable.about_product;
        ti.iconResHi = R.drawable.about_product_chect;
        ti.name = "产品指南";
        ti.activityname = "cn.etouch.ecalendar.pad";
        ti.cls = AboutProductActivity.class;
        mItemsList.add(ti);

        ti = new AboutItemInfo();
        ti.iconRes = R.drawable.about_company;
        ti.iconResHi = R.drawable.about_company_chect;
        ti.name = "公司介绍";
        ti.activityname = "cn.etouch.ecalendar.pad";
        ti.cls = AboutCompanyActivity.class;
        mItemsList.add(ti);

        ti = new AboutItemInfo();
        ti.iconRes = R.drawable.about_facility;
        ti.iconResHi = R.drawable.about_facility_chect;
        ti.name = "关于设备";
        ti.activityname = "cn.etouch.ecalendar.pad";
        ti.cls = AboutFacilityActivity.class;
        mItemsList.add(ti);
    }

    public void onCreate(Bundle saved) {
        super.onCreate(saved);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_tools);
        mAboutList = (ListView) findViewById(R.id.tools_list);
        mAboutList.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b == false) {
                    Log.i(TAG, " onFocusChange " + mAboutList.getSelectedItemPosition());
                    ImageView icon = (ImageView) view.findViewById(R.id.tool_icon);
                    icon.setImageResource(mItemsList.get(0).iconRes);
                }
            }
        });
        mAboutList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ImageView icon = (ImageView) view.findViewById(R.id.tool_icon);
                //icon.setImageResource(mItemsList.get(i).iconResHi);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        mAboutList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AboutItemInfo ti = mItemsList.get(i);
                Intent intent = new Intent(getApplicationContext(), ti.cls);
                startActivity(intent);
            }
        });
        mAboutList.setAdapter(new AboutItemAdapter());
    }

    private class AboutItemAdapter extends BaseAdapter {
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
                myview = AboutActivity.this.getLayoutInflater().inflate(R.layout.item_list_toolitem, parent, false);

                ImageView icon = (ImageView) myview.findViewById(R.id.tool_icon);
                TextView name = (TextView) myview.findViewById(R.id.tool_name);

                icon.setImageResource(mItemsList.get(pos).iconRes);
                name.setText(mItemsList.get(pos).name);
            } else {
                //holder = (ViewHolder) view.getTag();
            }

            return myview;
        }
    }
}
