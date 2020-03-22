package com.example.infection.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.infection.BuildConfig;
import com.example.infection.R;
import com.example.infection.utils.InfectedStateManager;
import com.example.infection.utils.TaskManager;

/**
 * Enters every 2 minutes and starts tasks and checks if users is cured.
 * todo there is a bug where the ui wont update (if you're currently on the ui) after you've been cured. However as this takes two weeks I find it unlikely that anyone would have the app open.
 */
public class AlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 12345;

    // Triggered by the Alarm periodically (starts the service to run task)
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.v(BuildConfig.APPLICATION_ID, "Starting Tasks and checking isCured state.");

        System.gc();
        InfectedStateManager infectedStateManager = new InfectedStateManager(context,null,false);



        // Checks if they were infected and their wait is over to be cured.
        //todo currently there is a bug where you immediately re-infect yourself as your old network services are still running.
        // This means that the first time that you're cured you can't be re-infected.
        if (InfectedStateManager.isCured(context) && infectedStateManager.getCurrentState().equals(context.getString(R.string.infected_tag))){

            new InfectedStateManager(context,context.getString(R.string.clean_tag),true);

        }else {
            TaskManager taskManager = new TaskManager();
            taskManager.run(context);
        }


    }
}
