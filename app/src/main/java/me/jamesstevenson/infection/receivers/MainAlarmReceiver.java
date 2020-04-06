package me.jamesstevenson.infection.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import me.jamesstevenson.infection.BuildConfig;
import me.jamesstevenson.infection.utils.TaskManager;

/**
 * Alarm receiver triggered when infected so that once duration is over can cure.
 */
public class MainAlarmReceiver extends BroadcastReceiver {

    public static int REQUEST_CODE = 98767867;
    String TAG = this.getClass().getSimpleName();

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {

        if (BuildConfig.DEBUG) {
            Log.v(TAG, "In Main Alarm Receiver");
        }
        TaskManager taskManager = new TaskManager();
        taskManager.startForeGroundService(context);

    }
}