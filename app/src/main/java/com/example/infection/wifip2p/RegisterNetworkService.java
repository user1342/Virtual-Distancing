package com.example.infection.wifip2p;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.example.infection.BuildConfig;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * This class registers network services used to identify nearby devices.
 */
public class RegisterNetworkService {

    final static String serviceType = "_http._tcp.";
    public static int localPort;
    private static NsdManager.RegistrationListener registrationListener;
    private String serviceName;
    private Context context;
    private NsdManager nsdManager;

    /**
     * When consytrcuted we attempt to un-register other network services, however, this will often fail.
     * @param context
     */
    public RegisterNetworkService(Context context) {
        this.context = context;
        unregisterService();
    }

    /**
     * Registering a service
     * @param port
     * @param state
     */
    public void registerService(int port, String state) {

        // The service name is our package name and the state of the device (clean, infected).
        serviceName = BuildConfig.APPLICATION_ID + "-" + state;

        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setServiceType(serviceType);
        serviceInfo.setPort(port);

        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

        if (nsdManager != null) {
            nsdManager.registerService(
                    serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
        }
    }


    /**
     * Attempts to unregister a service.
     */
    public void unregisterService() {

        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

        if (nsdManager != null) {
            try {
                nsdManager.unregisterService(registrationListener);
            } catch (Exception e) {
                // listener doesn't exist so can't remove
            }
        }
    }

    /**
     * Initialises the socket used for the network service server
     */
    public void initializeServerSocket() {
        // Initialize a server socket on the next available port.
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Store the chosen port.
        localPort = serverSocket.getLocalPort();
    }

    /**
     * Sets up the listener used as part of this.
     */
    public void initializeRegistrationListener() {
        registrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {

                serviceName = NsdServiceInfo.getServiceName();
                Log.v(BuildConfig.APPLICATION_ID, "Registration of Network Service " + serviceName);
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed! Put debugging code here to determine why.

                Log.v(BuildConfig.APPLICATION_ID, "Registration Failed: "+ errorCode);

            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered. This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
                Log.v(BuildConfig.APPLICATION_ID, "Uregistered Network Service Server");
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed. Put debugging code here to determine why.
                Log.v(BuildConfig.APPLICATION_ID, "Unregistering of Network Service Failed");
            }
        };
    }
}
