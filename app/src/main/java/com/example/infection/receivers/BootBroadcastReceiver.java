package com.example.infection.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.infection.BuildConfig;
import com.example.infection.utils.TaskManager;

/**
 * Starts up the application after device reboot.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

    /**
     * Receives the boot complete intent.
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action != null) {
            if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                Log.v(BuildConfig.APPLICATION_ID, "Starting Up, via BootComplete.");
                TaskManager taskManager = new TaskManager();
                taskManager.initialiseWork(context);
                //taskManager.run(context);
            }
        }
    }
}
