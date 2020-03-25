package com.example.infection.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.infection.BuildConfig;
import com.example.infection.R;
import com.example.infection.services.ForegroundServiceManager;
import com.example.infection.wifip2p.DiscoverServices;
import com.example.infection.wifip2p.RegisterNetworkService;

/**
 * This class is used for the tasking of work. This primarily involves the
 */
public class TaskManager {

    /**
     * This sets up the alarm manager.
     *
     * @param context
     */
    public void initialiseWork(Context context) {

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

        // Irrespective of state unregisters the current network service.
        RegisterNetworkService registerNetworkService = new RegisterNetworkService(context);
        registerNetworkService.unregisterService();

        String state = InfectedStateManager.getCurrentState(context); //Defaults to clean if doesn't have one.

        // If we're clean then we need to look for other networks, if we're infected we need to create a network.
        if (state.equals(context.getString(R.string.clean_tag))) {
            DiscoverServices discoverServices = new DiscoverServices(context);
            discoverServices.initializeDiscoveryListener();
            discoverServices.discover();
            discoverServices.removeDiscoveryListener();
        } else if (state.equals(context.getString(R.string.infected_tag))) {
            registerNetworkService.initializeServerSocket();
            registerNetworkService.initializeRegistrationListener();
            registerNetworkService.registerService(registerNetworkService.localPort, state);
        }


    }
}
