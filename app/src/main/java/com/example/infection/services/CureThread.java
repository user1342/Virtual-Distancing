package com.example.infection.services;

import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.infection.BuildConfig;
import com.example.infection.R;
import com.example.infection.utils.InfectedStateManager;
import com.example.infection.utils.TaskManager;

/**
 * This thread is called from the IsCuredAlarmReceiver to update the state when the device
 * is cured and to send a local broadcast receiver to the UI.
 */
public class CureThread extends Thread {

    private Context context;

    /**
     * Constructor
     *
     * @param context
     */
    public CureThread(Context context) {
        super();
        this.context = context;
    }

    /**
     * Entry point for the thread.
     */
    @Override
    public void run() {

        // Sets the state to cured
        InfectedStateManager infectedStateManager;
        infectedStateManager = new InfectedStateManager(context);
        infectedStateManager.setNewState(context.getString(R.string.clean_tag), true);

        // Updates the UI.
        Intent intent = new Intent(BuildConfig.APPLICATION_ID);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        // Sleep for 20 seconds before initialising (means can self infect).
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        TaskManager taskManager = new TaskManager();
        taskManager.initialiseWork(context);
    }
}