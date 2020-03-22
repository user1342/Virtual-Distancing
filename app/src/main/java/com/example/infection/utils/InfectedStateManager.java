package com.example.infection.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.infection.BuildConfig;
import com.example.infection.MainActivity;
import com.example.infection.R;
import com.example.infection.wifip2p.RegisterNetworkService;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class is used to manage the state of the user (infected, clean). In turn sending notifications, updating shared prefs, etc.
 */
public class InfectedStateManager {

    private static final long TWO_WEEKS_IN_MILLIS = 30000; // todo replace 1209600000
    private final String CHANNEL_ID = "341";
    private final String CHANNEL_NAME = BuildConfig.APPLICATION_ID + "Notification Chanel";
    private final String CHANNEL_DESC = "Used to updating users on their current state.";
    private final int NOTIFICATION_ID = 456;
    public String state;
    private Context context;

    /**
     * Constructor
     * @param context
     * @param newState
     * @param canRevert
     */
    public InfectedStateManager(Context context, String newState, boolean canRevert) {
        this.context = context;

        if (newState == null) {
            state = getCurrentState();
        } else {
            if (newState.contains(context.getString(R.string.infected_tag))) {
                state = context.getString(R.string.infected_tag);

                if (!getCurrentState().equals(state)) {
                    updateService(true); // Dont before updating the shared pref so that it can use the old name to de register the service
                    updateStateSharedPref();
                    updateRecouperationSharedPref();
                    sendPushNotification(context.getString(R.string.infected_tag).toLowerCase());

                }
            } else if (newState.contains(context.getString(R.string.clean_tag)) && canRevert) {
                state = context.getString(R.string.clean_tag);

                if (!getCurrentState().equals(state)) {
                    // Don't need to make a service for clean
                    updateService(false); // Dont before updating the shared pref so that it can use the old name to de register the service
                    updateStateSharedPref();
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
     * A static class to check that if the user if infected on if their recoup duration has ended. If so sends a notification.
     * @param context
     * @return boolean on if is cured
     */
    public static boolean isCured(Context context){
        // The below details how long the user needs before they can enter the cured state again (Two weeks from infection).
        // After that time if the user clicks that button they'll be cured.
        SharedPreferences sharedPref = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        long infectionStartDate = sharedPref.getLong(StringEnum.RECOUP_SHARED_PREF_KEY.getString(), 0); // Defaults to 0.
        long curedDate = infectionStartDate + TWO_WEEKS_IN_MILLIS; // Two weeks in milliseconds

        // Is cured?
        if (System.currentTimeMillis() >= curedDate){
            return true;
        } else {
            return false;
        }

    }

    /**
     * This function returns the date where the user will be cured.
     * @param context
     * @return string on the date the user will be cured.
     */
    public static String getCuredDate(Context context){
        SharedPreferences sharedPref = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        long infectionStartDate = sharedPref.getLong(StringEnum.RECOUP_SHARED_PREF_KEY.getString(), System.currentTimeMillis() + TWO_WEEKS_IN_MILLIS); //Defaults to clean if doesn't have one. Should never be reached, however, is set to value incase soft ock.
        long curedDate = infectionStartDate + TWO_WEEKS_IN_MILLIS; // Two weeks in milliseconds

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String curedDateAsString = formatter.format(new Date(curedDate));

        return curedDateAsString;

    }


    /**
     * Reads the shared preference to get the current state (infected, clean) of the user.
     * @return clean or infected.
     */
    public String getCurrentState() {

        SharedPreferences sharedPref = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        return sharedPref.getString(StringEnum.STATE_SHARED_PREF_KEY.getString(), context.getString(R.string.clean_tag));
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
     * Updates shared preference 'state'key with the new state.
     */
    private void updateStateSharedPref() {

        SharedPreferences sharedPref = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(StringEnum.STATE_SHARED_PREF_KEY.getString(), this.state);

        editor.apply();
    }

    /**
     * Sends a notification to the device on the change of state.
     * @param verb - infected, or clean.
     */
    private void sendPushNotification(String verb) {

        Log.v("hello test", "should send notification for: "+ verb);

        createNotificationChannel();

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_warning_black_24dp)
                .setContentTitle(state.toUpperCase())
                .setContentText(context.getString(R.string.you_are) + " "+ verb + context.getString(R.string.exclamation))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
        System.exit(0);
    }

    /**
     * On API 26+ a notification channel is required to send notifications to a device.
     */
    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            channel.setDescription(CHANNEL_DESC);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Removes the current network services and creates a new one for the new state.
     */
    private void updateService(boolean startNew) {
        RegisterNetworkService resisterNetworkService = new RegisterNetworkService(context);
        resisterNetworkService.unregisterService();

        if (startNew) {
            resisterNetworkService.initializeServerSocket();
            resisterNetworkService.initializeRegistrationListener();
            resisterNetworkService.registerService(resisterNetworkService.localPort, state);
        }
    }
}
