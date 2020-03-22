package com.example.infection.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.example.infection.BuildConfig;
import com.example.infection.R;
import com.example.infection.receivers.AlarmReceiver;
import com.example.infection.wifip2p.DiscoverServices;
import com.example.infection.wifip2p.RegisterNetworkService;

/**
 * This class is used for the tasking of work. This primarily involves the
 */
public class TaskManager {

    /**
     * The constrcutor.
     * @param context
     */
    private void initAlarmManager(Context context) {

        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(context, AlarmReceiver.class);

        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Setup periodic alarm every minute from this point onwards
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarm != null) {
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                    1 * 60 * 1000, pIntent);
        }
    }

    /**
     * This sets up the alarm manager.
     * @param context
     */
    public void initialiseWork(Context context) {

        initAlarmManager(context);
    }

    /**
     * Starts network service discovery and starts up a service.
     * @param context
     */
    public void run(Context context) {

        // Gets the current state from shared pref and adds that to register a service.
        SharedPreferences sharedPref = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        String state = sharedPref.getString(StringEnum.STATE_SHARED_PREF_KEY.getString(), context.getString(R.string.clean_tag)); //Defaults to clean if doesn't have one.



        // If we're clean then we need to look for other networks, if we're infected we need to create a network.
        if (state.equals(context.getString(R.string.clean_tag))) {
            DiscoverServices discoverServices = new DiscoverServices(context);
            discoverServices.initializeDiscoveryListener();
            discoverServices.discover();
            discoverServices.removeDiscoveryListener();
        } else{
            RegisterNetworkService resisterNetworkService = new RegisterNetworkService(context);
            resisterNetworkService.initializeServerSocket();
            resisterNetworkService.initializeRegistrationListener();
            resisterNetworkService.registerService(resisterNetworkService.localPort, state);
        }


    }
}
