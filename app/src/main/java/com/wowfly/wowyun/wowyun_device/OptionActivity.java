package com.wowfly.wowyun.wowyun_device;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
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
public class OptionActivity extends Activity {
    private ListView mOptionList;
    private static final String TAG = "OptionActivity";
    protected SharedPreferences mPref;

    public class OptionItemInfo {
        int iconRes;
        int iconResHi;
        String name;
        String activityname;
    }
    private ArrayList<OptionItemInfo> mItemsList;

    public OptionActivity() {
        mItemsList = new ArrayList<OptionItemInfo>();
        OptionItemInfo ti = new OptionItemInfo();
        ti.iconRes = R.drawable.set_family;
        ti.iconResHi = R.drawable.set_family_chect;
        ti.name = "家人动态";
        ti.activityname = "cn.etouch.ecalendar.pad";
        mItemsList.add(ti);

        ti = new OptionItemInfo();
        ti.iconRes = R.drawable.set_photo;
        ti.iconResHi = R.drawable.set_photo_chect;
        ti.name = "图片浏览";
        mItemsList.add(ti);

        ti = new OptionItemInfo();
        ti.iconRes = R.drawable.set_video;
        ti.iconResHi = R.drawable.set_video_chect;
        ti.name = "视频浏览";
        mItemsList.add(ti);

        ti = new OptionItemInfo();
        ti.iconRes = R.drawable.set_alarm;
        ti.iconResHi = R.drawable.set_alarm_chect;
        ti.name = "闹钟";
        mItemsList.add(ti);

        ti = new OptionItemInfo();
        ti.iconRes = R.drawable.set_data;
        ti.iconResHi = R.drawable.set_data_chect;
        ti.name = "时间和日期";
        mItemsList.add(ti);

        ti = new OptionItemInfo();
        ti.iconRes = R.drawable.set_wifi;
        ti.iconResHi = R.drawable.set_wifi_chect;
        ti.name = "无线WIFI";
        mItemsList.add(ti);

        ti = new OptionItemInfo();
        ti.iconRes = R.drawable.set_sound;
        ti.iconResHi = R.drawable.set_sound_chect;
        ti.name = "声音";
        mItemsList.add(ti);

        ti = new OptionItemInfo();
        ti.iconRes = R.drawable.set_brightness;
        ti.iconResHi = R.drawable.set_brightness_chect;
        ti.name = "亮度";
        mItemsList.add(ti);
    }

    private void createPhotoOptDialog() {
        AlertDialog dlg;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        int val = mPref.getInt("option.image.sorttype", 0);
        builder.setSingleChoiceItems(R.array.imageopt, val, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor editor = mPref.edit();
                editor.putInt("option.image.sorttype", i);
                editor.commit();
                switch (i) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
                dialogInterface.cancel();
            }
        });
        dlg = builder.create();
        dlg.show();
    }

    private void createVideoOptDialog() {
        AlertDialog dlg;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        int val = mPref.getInt("option.video.sorttype", 0);
        builder.setSingleChoiceItems(R.array.imageopt, val, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor editor = mPref.edit();
                editor.putInt("option.video.sorttype", i);
                editor.commit();
                switch (i) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
                dialogInterface.cancel();
            }
        });
        dlg = builder.create();
        dlg.show();
    }

    private void createSnsOptDialog() {
        AlertDialog dlg;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        int val = mPref.getInt("option.sns.sorttype", 0);
        builder.setSingleChoiceItems(R.array.snssortopt, val, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor editor = mPref.edit();
                editor.putInt("option.sns.sorttype", i);
                editor.commit();
                switch (i) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                }
                dialogInterface.cancel();
            }
        });
        dlg = builder.create();
        dlg.show();
    }


    public void onCreate(Bundle saved) {
        super.onCreate(saved);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_option);

        mOptionList = (ListView) findViewById(R.id.option_list);
        mOptionList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ImageView icon = (ImageView) view.findViewById(R.id.tool_icon);
                icon.setImageResource(mItemsList.get(i).iconResHi);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mOptionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.i(TAG, " item " + i + "clicked");
                switch (mItemsList.get(i).iconRes) {
                    case R.drawable.set_family:
                        createSnsOptDialog();
                        break;

                    case R.drawable.set_photo:
                        createPhotoOptDialog();
                        break;

                    case R.drawable.set_video:
                        createVideoOptDialog();
                        break;

                    case R.drawable.set_sound:
                        Intent _setting = new Intent(Settings.ACTION_SOUND_SETTINGS);
                        _setting.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(_setting);
                        break;
                    case R.drawable.set_wifi:
                        _setting = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        _setting.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(_setting);
                        break;

                    case R.drawable.set_brightness:
                        _setting = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
                        _setting.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(_setting);
                        break;
                    case R.drawable.set_data:
                        _setting = new Intent(Settings.ACTION_DATE_SETTINGS);
                        _setting.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(_setting);
                        break;
                }
            }
        });
        mOptionList.setAdapter(new OptionItemAdapter());
        mPref = this.getSharedPreferences("wowyun-device", Context.MODE_PRIVATE);
    }

    private class OptionItemAdapter extends BaseAdapter {
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
                myview = OptionActivity.this.getLayoutInflater().inflate(R.layout.item_list_toolitem, parent, false);

                ImageView icon = (ImageView) myview.findViewById(R.id.tool_icon);
                TextView name = (TextView) myview.findViewById(R.id.tool_name);

                icon.setImageResource(mItemsList.get(pos).iconRes);
                name.setText(mItemsList.get(pos).name);
            } else {
                ImageView icon = (ImageView) myview.findViewById(R.id.tool_icon);
                TextView name = (TextView) myview.findViewById(R.id.tool_name);

                icon.setImageResource(mItemsList.get(pos).iconRes);
                name.setText(mItemsList.get(pos).name);
            }

            return myview;
        }
    }
}
