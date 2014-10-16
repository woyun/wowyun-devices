package com.wowfly.wowyun.wowyun_device;

import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.xml.sax.XMLReader;

/**
 * Created by user on 9/30/14.
 */
public class SNSTagHandler implements Html.TagHandler {
    private static final String TAG = "SNSTagHandler";
    View container;
    int start;
    int stop;

    public SNSTagHandler(View myview) {
        container = myview;
    }
    public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
        //Log.i(TAG, " " + opening + " tag " + tag);
        if(tag.equalsIgnoreCase("qzonelogo")) {
            ImageView snslogo = (ImageView) container.findViewById(R.id.sns_logo);
            if(opening) {
                start = output.length();
            } else {
                stop = output.length();
                Drawable d = container.getResources().getDrawable(R.drawable.ic_logo_qzone_24x24);
                //Drawable d = getResources().getDrawable(R.drawable.ic_logo_weibo_24x24);
                //d.setBounds(0, 0, 24, 24);
                snslogo.setImageDrawable(d);
                Log.i(TAG, " set qzone sns logo");
            }
        } else if(tag.equalsIgnoreCase("weibologo")) {
            ImageView snslogo = (ImageView) container.findViewById(R.id.sns_logo);
            if(opening) {
                start = output.length();
            } else {
                stop = output.length();
                Drawable d = container.getResources().getDrawable(R.drawable.ic_logo_weibo_24x24);
                //Drawable d = getResources().getDrawable(R.drawable.ic_logo_weibo_24x24);
                //d.setBounds(0, 0, 24, 24);
                snslogo.setImageDrawable(d);
                Log.i(TAG, " set weibo sns logo");
            }
        }
    }
}
