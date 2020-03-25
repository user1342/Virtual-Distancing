package com.example.infection.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.infection.BuildConfig;
import com.example.infection.services.CureThread;

/**
 * Alarm receiver triggered when infected so that once duration is over can cure.
 */
public class IsCuredAlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {

        if (BuildConfig.DEBUG) {
            Log.v(BuildConfig.APPLICATION_ID, " In Is Cured Alarm Receiver");
        }

        // Starts thread.
        Thread thread = new CureThread(context);
        thread.start();
    }
}