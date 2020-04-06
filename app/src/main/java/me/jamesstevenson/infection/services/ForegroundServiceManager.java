package me.jamesstevenson.infection.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import me.jamesstevenson.infection.BuildConfig;
import me.jamesstevenson.infection.MainActivity;
import me.jamesstevenson.infection.R;
import me.jamesstevenson.infection.utils.InfectedStateManager;
import me.jamesstevenson.infection.utils.MultipleUse;
import me.jamesstevenson.infection.utils.TaskManager;

/**
 * This foreground service sets up the main service.
 * This gets set up one and then the listeners are used to run the needed code.
 */
public class ForegroundServiceManager extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Log.v(BuildConfig.APPLICATION_ID, "Foreground Service Is Now Running");
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        // Notification required for foreground service.
        Notification notification = new NotificationCompat.Builder(getApplicationContext(), MultipleUse.CHANNEL_ID)
                .setContentTitle(MultipleUse.getApplicationName(getApplicationContext()) + " Scanner")
                .setContentText("Checking for nearby infected devices...")
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Checks if cured and if so cures.
        if (InfectedStateManager.isCured(getApplicationContext()) && InfectedStateManager.getCurrentState(getApplicationContext()).equals(getApplicationContext().getString(R.string.infected_tag))) {

            InfectedStateManager infectedStateManager;
            infectedStateManager = new InfectedStateManager(getApplicationContext());
            infectedStateManager.setNewState(getApplicationContext().getString(R.string.clean_tag), true);

        }

        TaskManager thread = new TaskManager();
        thread.run(getApplicationContext());


        // Updates broadcast receiver if state has changed.
        // This updates the UI.
        Intent intent2 = new Intent(BuildConfig.APPLICATION_ID);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent2);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
