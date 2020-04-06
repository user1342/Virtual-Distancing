package com.example.infection.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.infection.BuildConfig;
import com.example.infection.R;
import com.example.infection.receivers.MainAlarmReceiver;
import com.example.infection.services.ForegroundServiceManager;

/**
 * This class is used for the tasking of work. This primarily involves the
 */
public class TaskManager {

    String TAG = this.getClass().getSimpleName();

    public void startPeriodicWork(Context context) {

        Log.v(TAG, "Starting periodic work");

        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(context, MainAlarmReceiver.class);

        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, MainAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Setup periodic alarm every every half hour from this point onwards
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY

        if (alarm != null) {
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                    60 * 1000, pIntent);
        }

    }


    /**
     * This sets up the alarm manager.
     *
     * @param context
     */
    public void startForeGroundService(Context context) {

        Log.v(TAG, "Starting foreground service");

        Intent i = new Intent(context, ForegroundServiceManager.class);
        // Start the service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (BuildConfig.DEBUG) {
                Log.v(BuildConfig.APPLICATION_ID, "Starting Foreground Service");
            }
            context.startForegroundService(i);
        } else {
            if (BuildConfig.DEBUG) {
                Log.v(BuildConfig.APPLICATION_ID, "Starting Background Service");
            }
            context.startService(i);
        }

    }

    /**
     * Starts network service discovery and starts up a service.
     *
     * @param context
     */
    public void run(Context context) {

        // Checks if cured and if so cures.
        if (InfectedStateManager.isCured(context) && InfectedStateManager.getCurrentState(context).equals(context.getString(R.string.infected_tag))) {

            InfectedStateManager infectedStateManager;
            infectedStateManager = new InfectedStateManager(context);
            infectedStateManager.setNewState(context.getString(R.string.clean_tag), true);

        } else {
            // set state to be current state
            String currentState = InfectedStateManager.getCurrentState(context);
            InfectedStateManager infectedStateManager = new InfectedStateManager(context);
            infectedStateManager.setNewState(currentState, true);
        }


    }
}
