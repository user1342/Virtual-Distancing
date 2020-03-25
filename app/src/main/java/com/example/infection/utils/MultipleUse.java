package com.example.infection.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.example.infection.BuildConfig;

public class MultipleUse {
    public static final String CHANNEL_ID = "341";
    public static final int NOTIFICATION_ID = 456;
    private static final String CHANNEL_NAME = BuildConfig.APPLICATION_ID + "Notification Chanel";
    private static final String CHANNEL_DESC = "Used to updating users on their current state.";

    /**
     * On API 26+ a notification channel is required to send notifications to a device.
     */
    public static void createNotificationChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, MultipleUse.CHANNEL_NAME, importance);
            channel.setDescription(MultipleUse.CHANNEL_DESC);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

}
