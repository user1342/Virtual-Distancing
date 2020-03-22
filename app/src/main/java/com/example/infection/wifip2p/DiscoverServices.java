package com.example.infection.wifip2p;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.example.infection.BuildConfig;
import com.example.infection.utils.InfectedStateManager;

/**
 * This class is responsible for discovering other network services.
 */
public class DiscoverServices {

    private Context context;
    private NsdManager nsdManager;
    private NsdManager.DiscoveryListener discoveryListener;

    /**
     * Constrcutor
     * @param context
     */
    public DiscoverServices(Context context) {
        this.context = context;
        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
    }

    /**
     * Discovers nearby network services
     */
    public void discover() {
        nsdManager.discoverServices(
                RegisterNetworkService.serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    /**
     * Removes the listener.
     */
    public void removeDiscoveryListener() {
        discoveryListener = null;
    }

    /**
     * Initialises the listener.
     */
    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(BuildConfig.APPLICATION_ID, "Started discovery for network services.");
            }

            /**
             * Run when a service is found. This checks if the service is part of this app and if so checks it's state.
             * If it is infected then the InfectedStateManager is run.
             * @param service
             */
            @Override
            public void onServiceFound(NsdServiceInfo service) {
                String serviceName = service.getServiceName();

                // One of our services has been found.
                if (serviceName.startsWith(BuildConfig.APPLICATION_ID)) {
                    Log.v(BuildConfig.APPLICATION_ID, "Found Service: " + serviceName);
                    new InfectedStateManager(context, serviceName, false);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(BuildConfig.APPLICATION_ID, "Service Lost: " + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(BuildConfig.APPLICATION_ID, "Discovery Stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(BuildConfig.APPLICATION_ID, "Discovery failed: Error code:" + errorCode);
                try {
                    nsdManager.stopServiceDiscovery(this);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(BuildConfig.APPLICATION_ID, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        };
    }
}
