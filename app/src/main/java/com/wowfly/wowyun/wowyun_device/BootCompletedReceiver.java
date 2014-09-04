package com.wowfly.wowyun.wowyun_device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by user on 8/27/14.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent arg) {
        Intent intent = new Intent();
        intent.setClassName("com.wowfly.wowyun.wowyun_device", "com.wowfly.wowyun.wowyun_device.MainActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
