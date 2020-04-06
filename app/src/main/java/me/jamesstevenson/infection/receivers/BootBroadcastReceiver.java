package me.jamesstevenson.infection.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import me.jamesstevenson.infection.BuildConfig;
import me.jamesstevenson.infection.utils.TaskManager;
import me.jamesstevenson.infection.wifip2p.DiscoverWifiP2PService;

/**
 * Starts up the application after device reboot.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {

    /**
     * Receives the boot complete intent.
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action != null) {
            if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {

                if (BuildConfig.DEBUG) {
                    Log.v(BuildConfig.APPLICATION_ID, "Starting Up, via BootComplete.");
                }

                DiscoverWifiP2PService discoverWifiP2PService = new DiscoverWifiP2PService(context);
                discoverWifiP2PService.setupDiscovery();

                TaskManager taskManager = new TaskManager();
                taskManager.startPeriodicWork(context);
                taskManager.run(context);
            }

            // Updates broadcast receiver if state has changed.
            // This updates the UI.
            Intent intent2 = new Intent(BuildConfig.APPLICATION_ID);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
        }
    }
}
