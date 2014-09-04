package com.wowfly.wowyun.wowyun_device;

import com.wowfly.wowyun.wowyun_device.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextClock;
import android.widget.TextView;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;
import org.w3c.dom.Text;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.Security;

import zh.wang.android.apis.yweathergetter4a.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.TimeZone;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends Activity implements YahooWeatherInfoListener, YahooWeatherExceptionListener, ChatManagerListener {
    private static final String TAG = "MainActivity";
    private WhellMenuView mView;
    private WifiManager mWiFi;
    private ConnectivityManager cm = null;
    private ListView mWiFiListView = null;
    private String mDeviceID = "T";
    private SharedPreferences mPref;
    private WowYunApp mAPP;
    private XMPPService mXMPP;
    public Handler mHandler;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    private boolean mIsConnected;
    private int mBatteryLevel;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    private YahooWeather mWeather = YahooWeather.getInstance(5000,5000, true);

    private WifiReceiver mWifiReceiver;
    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;


    private RosterListener mRosterListener = new RosterListener() {
        @Override
        public void entriesAdded(Collection<String> strings) {
            for(String item: strings) {
                Log.i(TAG, " buddy added " + item);
            }
        }

        @Override
        public void entriesUpdated(Collection<String> strings) {
            for(String item: strings) {
                Log.i(TAG, " buddy entriesUpdated " + item);
            }
        }

        @Override
        public void entriesDeleted(Collection<String> strings) {
            for(String item: strings) {
                Log.i(TAG, " buddy entriesDeleted " + item);
            }
        }

        @Override
        public void presenceChanged(Presence presence) {
            Log.i(TAG, " presenceChanged " + presence.getType().toString() + " jid = " + presence.getFrom());
        }
    };

    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            List <Map<String, Object>> _wifiAPList = new ArrayList<Map<String, Object>>();

            List<ScanResult> _wifiList = mWiFi.getScanResults();
            for(int idx=0; idx<_wifiList.size(); idx++) {
                if(_wifiList.get(idx).SSID.length() <=0)
                    continue;

                Map<String, Object> item = new HashMap<String, Object>();
                item.put("ssid", _wifiList.get(idx).SSID);
                item.put("bssid", _wifiList.get(idx).BSSID);
                item.put("apinfo", _wifiList.get(idx));
                int level = _wifiList.get(idx).level;
                if(level <  0 && level >= -50) {
                    item.put("level", R.drawable.stat_sys_wifi_signal_4_fully);
                }else if(level >= -70 && level < - 50) {
                    item.put("level", R.drawable.stat_sys_wifi_signal_3_fully);
                } else if (level >= -80 && level < -70) {
                    item.put("level", R.drawable.stat_sys_wifi_signal_2_fully);
                } else if (level < -80) {
                    item.put("level", R.drawable.stat_sys_wifi_signal_0);
                }
                _wifiAPList.add(item);
                //Log.i(TAG, " ssid = " + _wifiList.get(idx).SSID + " level = " + _wifiList.get(idx).level);
            }

            View layout = (View) mWiFiListView.getParent();
            layout = layout.findViewById(R.id.wifi_scanning_layout);
            layout.setVisibility(View.GONE);

            SimpleAdapter sa = new SimpleAdapter(getApplicationContext(), _wifiAPList, R.layout.item_wifi_list,
                    new String[] {"level", "ssid"},
                    new int[] {R.id.wifi_level, R.id.wifi_ssid});
            mWiFiListView.setAdapter(sa);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.i(TAG, "Screen orientation is ORIENTATION_LANDSCAPE");

        } else if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.i(TAG, "Screen orientation is ORIENTATION_PORTRAIT");
        }
    }

    public void onBackPressed() {
        Log.i(TAG, " ignore back key pressed");
        if(mSystemUiHider.isVisible())
          mSystemUiHider.hide();
    }

    public void chatCreated(Chat chat, boolean create) {
        //Log.i(TAG, " chat = " + chat.toString());
        chat.addMessageListener(new MessageListener() {
            @Override
            public void processMessage(Chat chat, org.jivesoftware.smack.packet.Message message) {
                Log.i(TAG, "from_jid = " + message.getFrom() + " body = " + message.getBody());
                String msg = message.getBody();
                if(msg.startsWith("image/")) {
                    //mView.
                    mView.incNotificationNumber(1);

                } else if(msg.startsWith("video/")) {
                    mView.incNotificationNumber(0);
                }
            }
        });
    }

    private void setDateText(TextView weekTV, TextView dateTV) {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String year = String.valueOf(c.get(Calendar.YEAR));
        String month = String.valueOf(1+c.get(Calendar.MONTH));
        String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));

        String week = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
        String weekText = "星期";
        switch(Integer.parseInt(week)) {
            case 1:
                weekText += "天";
                break;
            case 2:
                weekText += "一";
                break;
            case 3:
                weekText += "二";
                break;
            case 4:
                weekText += "三";
                break;
            case 5:
                weekText += "四";
                break;
            case 6:
                weekText += "五";
                break;
            case 7:
                weekText += "六";
                break;
        }
        weekTV.setText(weekText);
        dateTV.setText(year + "年" + month+ "月" + day + "日");
    }

    private String generateDeviceID(String mac) {

        String[] macitem = mac.split(":");
        Log.i(TAG, " mac = " + mac + " macitems " + macitem);
        for(String item: macitem) {
            int val = Integer.parseInt(item, 16);
            if(val > 0) {
                mDeviceID += val;
            }
            //Log.i(TAG, " val = " + val);
        }
        mDeviceID += "9";
        Log.i(TAG, " deviceID = " + mDeviceID);
        TextView deviceID = (TextView) findViewById(R.id.deviceid);
        //deviceID.setTextColor(android.R.color.holo_green_dark);
        deviceID.setText(getResources().getString(R.string.deviceid) + " " + mDeviceID);

        return mDeviceID;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        //Log.i(TAG, "new key code = " + keyCode);
        switch(keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                mView.goPrevItem();
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                mView.goNextItem();
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                int selected = mView.getSelectedPosition();
                Log.i(TAG, "goto Item " + selected);
                gotoActivity(selected);
                mView.setNotificationNumber(selected, 0);
                break;
            case KeyEvent.KEYCODE_BACK:
                return true;
        }

        if(mSystemUiHider.isVisible())
            mSystemUiHider.hide();

/*        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Log.i(TAG, "Volume up key event");
            mView.goNextItem();
        }
        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Log.i(TAG, "Volume down key event");
            mView.goPrevItem();
        }*/

        return super.onKeyDown(keyCode, event);
    }

    private void gotoActivity(int position) {
        //Intent intent = new Intent(this, )
        Class<?> cls = null;
        switch(position) {
            case 0:
                cls = VideoBrowserActivity.class;
                break;
            case 1:
                cls = ImageBrowserActivity.class;
                break;
            default:
                return;
        }

        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }

    public void gotWeatherInfo(WeatherInfo weatherInfo) {
        if(weatherInfo != null) {
            String weather = weatherInfo.getCurrentText();
            ImageView weatherView = (ImageView) findViewById(R.id.weather_icon);
            //weatherView.setImageBitmap(weatherInfo.getCurrentConditionIcon());
            weatherView.setImageResource(getWeatherIconResourceID(weather));
            TextView textView = (TextView) findViewById(R.id.weather_city);
            textView.setText("HangZhou");
            textView = (TextView) findViewById(R.id.weather_info);
            textView.setText(weather);
        }
    }

    public void onFailConnection(final Exception e) {

    }

    public void onFailParsing(final Exception e) {
        Log.i(TAG, "parsing exception = " + e.getMessage());
    }

    public void onFailFindLocation(final Exception e) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WhellMenuView view ;
        WowYunApp _app = (WowYunApp) getApplication();

        super.onCreate(savedInstanceState);
        Point size = new Point();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mView = (WhellMenuView )findViewById(R.id.whellmenu);
        getWindowManager().getDefaultDisplay().getSize(size);
        mView.setWhellCount(7, size.y, size.x);
        //mView.setNotificationNumber(1, 6);

        mWeather.setExceptionListener(this);
        final TextClock textClock = (TextClock) findViewById(R.id.textclock);
        TextView weekText = (TextView) findViewById(R.id.main_week_text);
        //weekText.setText(getWeekText());
        TextView dateText = (TextView) findViewById(R.id.main_full_date);
        setDateText(weekText, dateText);

        TextView lunarText = (TextView) findViewById(R.id.main_lunar_text);
        lunarText.setText("农历 九月十一");

        //LocationManager locMan = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        //long networkTS = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getTime();
        //Log.i(TAG, "Current network time = " + networkTS);

        //textClock.
        //final View controlsView = findViewById(R.id.fullscreen_content_controls);
        //final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        final View mView = findViewById(R.id.wowyunbar);
        mSystemUiHider = SystemUiHider.getInstance(this, mView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider.hide();

        //Log.i(TAG, "start yahoo weather ");
/*        mWeather.setNeedDownloadIcons(true);
        mWeather.setSearchMode(YahooWeather.SEARCH_MODE.GPS);
        mWeather.queryYahooWeatherByGPS(getApplicationContext(), this);*/

        mWiFi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        Log.i(TAG, "WifiState = " + mWiFi.getWifiState());

        mWiFi.setWifiEnabled(true);
        WifiInfo wi = mWiFi.getConnectionInfo();
        _app.deviceID = generateDeviceID(wi.getMacAddress());
        //Log.i(TAG, " rssi = " + wi.getRssi() + " ssid = " +  wi.getNetworkId() +" " + wi.getSSID() + " mac = " + wi.getMacAddress() + " signal level = " + mWiFi.calculateSignalLevel(wi.getRssi(), 5));
        //Log.i(TAG, "AndroidID = " + Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID));
        //Log.i(TAG, "device ID = " + );
        //Log.i(TAG, "bluetooth address = " + );
        //android.R.drawable.

        mWeather.setNeedDownloadIcons(false);
        mWeather.setSearchMode(YahooWeather.SEARCH_MODE.PLACE_NAME);
        mWeather.queryYahooWeatherByPlaceName(getApplicationContext(), "杭州", this);
        //mWeather.
        //mWeather.

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, intentFilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        Log.i(TAG, "battery status = " + level + "  scale = " + scale);
        mBatteryLevel = (int) Math.round((level * 100.0)/scale);
        ImageView img = (ImageView)findViewById(R.id.battery_status);
        img.setImageResource(getBatteryStatusResourceID(mBatteryLevel));
        img = (ImageView) findViewById(R.id.wifi_status);
        img.setImageResource(getWifiStatusResourceID(mWiFi.calculateSignalLevel(wi.getRssi(), 5)));

        cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        mIsConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if(mIsConnected == false) {
            showWifiManagerDialog();
        }

        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                //ImageView statusIcon = (ImageView)findViewById(R.id.login_status_icon);
                switch (msg.what) {
                    case WowYunApp.ACTIVITY_DESTROY:
                        if(mSystemUiHider.isVisible())
                            mSystemUiHider.hide();
                        break;

                    case WowYunApp.XMPP_REGISTER_FAILURE:
                        Log.i(TAG, "message XMPP_REGISTER_FAILURE");
                        break;
                    case WowYunApp.XMPP_REGISTER_SUCCESS:
                        Log.i(TAG, "message XMPP_REGISTER_SUCCESS ");
                    case WowYunApp.XMPP_DO_LOGIN:
                        Runnable loginRunnable = new Runnable() {
                            @Override
                            public void run() {
                                if(mXMPP == null) {
                                    mXMPP = mAPP.getXMPP();
                                }
                                boolean ret = mXMPP.doLogin(mDeviceID, mDeviceID, MainActivity.this);
                                if(ret == true) {
                                    Message msg = new Message();
                                    msg.what = WowYunApp.XMPP_LOGIN_SUCCESS;
                                    MainActivity.this.mHandler.sendMessage(msg);
                                }
                                Log.i(TAG, " " + mDeviceID + " login status " + ret);

                            }
                        };
                        new Thread(loginRunnable).start();
                        break;
                    case WowYunApp.XMPP_LOGIN_SUCCESS:
                        TextView deviceID = (TextView) findViewById(R.id.deviceid);
                        deviceID.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                        //deviceID.setText(mDeviceID);
                        break;
                }
                super.handleMessage(msg);
            }
        };

        mPref = getSharedPreferences("wowyun-mobile", MODE_PRIVATE);
        final boolean isRegistered = mPref.getBoolean(new String("device.status.registered"), false);
        Log.i(TAG, "device.status.registered = " + isRegistered);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mAPP = (WowYunApp)getApplication();
                do {
                    mXMPP = mAPP.getXMPP();
                    if(mXMPP == null) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            Log.i(TAG, "sleep Exception " + e.getMessage());
                            continue;
                        }
                        Log.i(TAG, "Thread goto sleep, for XMPP service initialize");
                    }
                } while(mXMPP == null);
                Log.i(TAG, " XMPP = " + mXMPP);
                mXMPP.addRosterListener(MainActivity.this.mRosterListener);
                if(! isRegistered) {
                    boolean ret = mXMPP.doRegister(mDeviceID, mDeviceID);
                    Message msg = new Message();
                    if (ret == true) {
                        SharedPreferences.Editor editor = mPref.edit();
                        editor.putBoolean("device.status.registered", ret);
                        editor.commit();
                        msg.what = WowYunApp.XMPP_REGISTER_SUCCESS;
                    } else {
                        msg.what = WowYunApp.XMPP_REGISTER_FAILURE;
                        Log.i(TAG, "auto register XMPP account failure");
                    }
                    MainActivity.this.mHandler.sendMessage(msg);
                } else {
                    Message msg = new Message();
                    msg.what = WowYunApp.XMPP_DO_LOGIN;
                    MainActivity.this.mHandler.sendMessage(msg);
                }
            }
        };
        new Thread(runnable).start();
        //mView.setNotificationNumber(1, 9);


    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, " onLocationChanged " + location.getLatitude() + " " + location.getLongitude());
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private void getLocationFromWiFi() {
        double lat, lon;
        LocationManager localMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(false);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        criteria.setSpeedRequired(false);
        //localMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        String bestProvider = localMgr.getBestProvider(criteria, true);

        Location location = localMgr.getLastKnownLocation(bestProvider);
        if(location == null) {
            localMgr.requestLocationUpdates(bestProvider, 0, 0, locationListener);
        } else {
            lat = location.getLatitude();
            lon = location.getLongitude();
            Log.i(TAG, " get location success ( lat = " + lat + " lon = " + lon + ")");
        }
    }

    private int getWeatherIconResourceID(String weather) {
        Field field;
        int ID = R.drawable.default_weather_icon;

        weather = weather.replace(' ', '_').toLowerCase();

        try {
            field = R.drawable.class.getField(weather);
            ID = field.getInt(new R.drawable());
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }

        return ID;
    }

    private int getWifiStatusResourceID(int level) {
        Field field;
        int ID = R.drawable.stat_sys_wifi_signal_0;

        try{
            if(level == 0) {
                return R.drawable.stat_sys_wifi_signal_0;
            } else {
                field = R.drawable.class.getField("stat_sys_wifi_signal_" + level + "_fully");
                ID = field.getInt(new R.drawable());
            }
        } catch (NoSuchFieldException e) {
            Log.i(TAG, "resource is unavailable");
        } catch (IllegalAccessException e) {
            Log.i(TAG, "resource is unavailable");
        }
        return ID;
    }

    private int getBatteryStatusResourceID(int level) {
        if(level > 0 && level <= 15) {
            return R.drawable.stat_sys_battery_15;
        } else if(level > 15 && level <= 28) {
            return R.drawable.stat_sys_battery_28;
        } else if(level > 28 && level <=43 ) {
            return R.drawable.stat_sys_battery_43;
        } else if(level > 43 && level <= 57 ) {
            return R.drawable.stat_sys_battery_57;
        } else if(level > 57 && level <= 71) {
            return R.drawable.stat_sys_battery_71;
        } else if(level > 71 && level <=85 ) {
            return R.drawable.stat_sys_battery_85;
        } else if(level > 85 && level <= 100) {
            return R.drawable.stat_sys_battery_100;
        }

        return R.drawable.stat_sys_battery_0;
    }

    private void connectToAP(ScanResult sr, String passwd) {
        WifiConfiguration wc = new WifiConfiguration();
        wc.SSID = '\"' + sr.SSID + '\"';
        wc.hiddenSSID = true;
        wc.status = WifiConfiguration.Status.ENABLED;
        wc.priority = 40;
        //wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);

        wc.preSharedKey = '\"' + passwd + '\"';
        Log.i(TAG, " ssid = " + wc.SSID + " sharedKey = "+ wc.preSharedKey);
        int res = mWiFi.addNetwork(wc);
        boolean ret = mWiFi.enableNetwork(res, true);
        Log.i(TAG, " ssid = " + wc.SSID + " sharedKey = "+ wc.preSharedKey + " ret = " + ret);
    }

    private void showWifiManagerDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_wifi_manager);

        if(mWiFi.isWifiEnabled() == false) {
            mWiFi.setWifiEnabled(true);
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mWifiReceiver = new WifiReceiver();

                registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                mWiFi.startScan();
            }
        }, 1000);

        final LayoutInflater inflater = getLayoutInflater();
        final View layout = inflater.inflate(R.layout.dialog_wifi_manager, null);
        builder.setView(layout);

        final AlertDialog dlg = builder.create();
        dlg.show();
        dlg.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                unregisterReceiver(mWifiReceiver);
            }
        });

        mWiFiListView = (ListView) layout.findViewById(R.id.dialog_listview_wifi);
/*        SimpleAdapter sa = new SimpleAdapter(this, this.getWifiDataList(), R.layout.item_wifi_list,
                new String[] {"level", "ssid"},
                new int[] {R.id.wifi_level, R.id.wifi_ssid});*/
        //wifiList.setAdapter(sa);
        mWiFiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Map<String, Object> item = (Map<String, Object>)adapterView.getAdapter().getItem(i);
                final ScanResult sr = (ScanResult) item.get(new String("apinfo"));

                LayoutInflater _inflater = getLayoutInflater();
                View _layout = inflater.inflate(R.layout.dialog_password_input, null);
                final EditText _input = (EditText) _layout.findViewById(R.id.dialog_password_input_edit);

                //final EditText input = new EditText(getApplicationContext());
                Log.i(TAG, "sr.ssid = " + sr.SSID + " " + sr.capabilities);
                ((TextView) _layout.findViewById(R.id.dialog_password_input_tips)).setText(sr.capabilities);
                //Log.i(TAG, "wifi list item clicked " + i + " " + item.get(new String("ssid")));
                final AlertDialog.Builder pwdBuilder = new AlertDialog.Builder(MainActivity.this);

                pwdBuilder.setTitle(sr.SSID + " " + R.string.input_password )
                        .setView(_layout)
                        .setPositiveButton(R.string.action_connect, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Editable text = _input.getText();
                                String passwd = text.toString();
                                Log.i(TAG, " try to connect " + sr.SSID + " passwd = " + passwd);
                                connectToAP(sr, passwd);
                            }
                        }).setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
                //dlg.cancel();
            }
        });
    }

    private List<Map<String, String>> getWifiDataList() {
        return null;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        //delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
