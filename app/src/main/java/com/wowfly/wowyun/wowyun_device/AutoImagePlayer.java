package com.wowfly.wowyun.wowyun_device;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ViewFlipper;

/**
 * Created by user on 9/22/14.
 */
public class AutoImagePlayer extends Activity {

    private ViewFlipper viewFlipper;

    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        setContentView(R.layout.autoimageplayer);

        viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
        viewFlipper.setAutoStart(true);
        viewFlipper.setFlipInterval(2000);

    }
}
