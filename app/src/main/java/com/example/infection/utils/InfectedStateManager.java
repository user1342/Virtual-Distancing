package com.example.infection.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.infection.BuildConfig;
import com.example.infection.MainActivity;
import com.example.infection.R;
import com.example.infection.receivers.IsCuredAlarmReceiver;
import com.example.infection.wifip2p.DiscoverWifiP2PService;
import com.example.infection.wifip2p.RegisterWifiP2PService;

/**
 * This class is used to manage the state of the user (infected, clean). In turn sending notifications, updating shared prefs, etc.
 */
public class InfectedStateManager {

    private static final long TWO_WEEKS_IN_MILLIS = 1209600000;

    public String state;
    private Context context;

    /**
     * Constructor
     *
     * @param context
     */
    public InfectedStateManager(Context context) {
        this.context = context;
    }

    /**
     * A static class to check that if the user if infected on if their recoup duration has ended. If so sends a notification.
     *
     * @param context
     * @return boolean on if is cured
     */
    public static boolean isCured(Context context) {
        // The below details how long the user needs before they can enter the cured state again (Two weeks from infection).
        // After that time if the user clicks that button they'll be cured.
        SharedPreferences sharedPref = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        long infectionStartDate = sharedPref.getLong(StringEnum.RECOUP_SHARED_PREF_KEY.getString(), 0); // Defaults to 0.
        long curedDate = infectionStartDate + TWO_WEEKS_IN_MILLIS; // Two weeks in milliseconds

        // Is cured?
        if (System.currentTimeMillis() >= curedDate) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * This function returns the date where the user will be cured.
     *
     * @param context
     * @return string on the date the user will be cured.
     */
    public static long getCuredDate(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        long infectionStartDate = sharedPref.getLong(StringEnum.RECOUP_SHARED_PREF_KEY.getString(), System.currentTimeMillis() + TWO_WEEKS_IN_MILLIS); //Defaults to clean if doesn't have one. Should never be reached, however, is set to value incase soft ock.
        long curedDate = infectionStartDate + TWO_WEEKS_IN_MILLIS; // Two weeks in milliseconds

        return curedDate;

    }

    /**
     * Reads the shared preference to get the current state (infected, clean) of the user.
     *
     * @return clean or infected.
     */
    public static String getCurrentState(Context context) {

        SharedPreferences sharedPref = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return sharedPref.getString(StringEnum.STATE_SHARED_PREF_KEY.getString(), context.getString(R.string.clean_tag));
    }

    /**
     * Updates shared preference 'state'key with the new state.
     */
    private static void updateStateSharedPref(Context context, String state) {

        SharedPreferences sharedPref = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(StringEnum.STATE_SHARED_PREF_KEY.getString(), state);

        editor.apply();
    }

    /**
     * Updates the state of the device.
     *
     * @param newState
     * @param canRevert
     */
    public void setNewState(String newState, boolean canRevert) {

        if (newState == null) {
            state = getCurrentState(context);
        } else {
            if (newState.contains(context.getString(R.string.infected_tag))) {

                if (BuildConfig.DEBUG) {
                    Log.v(BuildConfig.APPLICATION_ID, "Setting state to infected");
                }

                state = context.getString(R.string.infected_tag);

                if (!getCurrentState(context).equals(state)) {
                    // don't create new service unless was first time changed from cured to infected.
                    updateService(true); // Dont before updating the shared pref so that it can use the old name to de register the service
                    updateStateSharedPref(context, state);
                    updateRecouperationSharedPref();
                    setCureAlarmManager(TWO_WEEKS_IN_MILLIS);
                    sendPushNotification(context.getString(R.string.infected_tag).toLowerCase());

                }
            } else if (newState.contains(context.getString(R.string.clean_tag)) && canRevert) {

                //unregister any current services as we're clean
                RegisterWifiP2PService resisterNetworkService = new RegisterWifiP2PService(context);


                resisterNetworkService.unRegister();
                if (BuildConfig.DEBUG) {
                    Log.v(BuildConfig.APPLICATION_ID, "Setting state to clean");
                }

                // Make sure that if cured discover
                DiscoverWifiP2PService discoverWifiP2PService = new DiscoverWifiP2PService(context);
                discoverWifiP2PService.discovery();

                state = context.getString(R.string.clean_tag);

                if (!getCurrentState(context).equals(state)) {
                    // Don't need to make a service for clean
                    updateService(false); // Dont before updating the shared pref so that it can use the old name to de register the service
                    updateStateSharedPref(context, state);
                    sendPushNotification(context.getString(R.string.clean_tag).toLowerCase());
                }
            }

            // Updates broadcast receiver if state has changed.
            // This updates the UI.
            Intent intent = new Intent(BuildConfig.APPLICATION_ID);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    /**
     * Adds the date where the user got infected to the shared pref.
     */
    private void updateRecouperationSharedPref() {

        SharedPreferences sharedPref = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(StringEnum.RECOUP_SHARED_PREF_KEY.getString(), System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Sends a notification to the device on the change of state.
     *
     * @param verb - infected, or clean.
     */
    private void sendPushNotification(String verb) {

        MultipleUse.createNotificationChannel(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MultipleUse.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle(state.toUpperCase())
                .setContentText(context.getString(R.string.you_are) + " " + verb + context.getString(R.string.exclamation))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(MultipleUse.NOTIFICATION_ID, builder.build());
    }


    /**
     * Removes the current network services and creates a new one for the new state.
     */
    private void updateService(boolean startNew) {

        RegisterWifiP2PService resisterNetworkService = new RegisterWifiP2PService(context);

        if (startNew) {
            resisterNetworkService.startRegistration();
        }
    }

    private void setCureAlarmManager(long milliseconsUntillCured) {

        if (BuildConfig.DEBUG) {
            Log.v(BuildConfig.APPLICATION_ID, "Creating Alarm Manager To Trigger When It's Time To Cure");
        }

        Intent intent = new Intent(context, IsCuredAlarmReceiver.class);

        final PendingIntent pIntent = PendingIntent.getBroadcast(context, IsCuredAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);


        if (alarm != null) {
            alarm.setExact(AlarmManager.RTC_WAKEUP, getCuredDate(context), pIntent);
        }


    }

}
