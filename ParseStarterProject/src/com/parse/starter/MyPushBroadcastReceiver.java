package com.parse.starter;

/**
 * Created by Hamza on 4/5/2015.
 */

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.ParseBroadcastReceiver;
import com.parse.ParsePushBroadcastReceiver;


public class MyPushBroadcastReceiver extends ParsePushBroadcastReceiver {
    private static final String TAG = "MyPushBroadcastReceiever";
    @Override
    protected Class<? extends Activity>getActivity(Context context, Intent intent)
    {
        return DistressListActivity.class;
    }
   /* @Override
    public void onReceive(Context context, Intent intent) {

            if (intent == null)
            {
               // Log.d(TAG, "Receiver intent null");
            }
            else
            {
                String action = intent.getAction();
                //Log.d(TAG, "got action " + action );
                if (action.equals("com.parse.starter.UPDATE_STATUS"))
                {
                    Intent i = new Intent(context,DistressListActivity.class);
                    context.startActivity(i);
                }
            }
        }*/
}