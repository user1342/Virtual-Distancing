package com.example.infection.wifip2p;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.provider.Settings;
import android.util.Log;

import com.example.infection.BuildConfig;
import com.example.infection.R;
import com.example.infection.utils.MultipleUse;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

public class RegisterWifiP2PService {

    static WifiP2pManager.Channel channel;
    static WifiP2pManager manager;
    static WifiP2pDnsSdServiceInfo serviceInfo;
    Context context;
    String TAG = this.getClass().getSimpleName();

    public RegisterWifiP2PService(Context context) {
        this.unRegister();

        this.manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        this.channel = manager.initialize(context, context.getMainLooper(), null);
        this.context = context;

        System.gc();
    }

    private static int getPort() {
        // Initialize a server socket on the next available port.
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Store the chosen port.
        return serverSocket.getLocalPort();
    }

    public void unRegister() {
        if (channel != null) {

            if (manager != null) {
                manager.clearLocalServices(channel, null);

                if (serviceInfo != null) {
                    Log.v(TAG, "Service info not null, attemping to remove network service.");
                    manager.removeLocalService(channel, serviceInfo, null);
                }

                manager.cancelConnect(channel, null);
                manager.removeGroup(channel, null);
                manager.clearServiceRequests(channel, null);
                manager.stopPeerDiscovery(channel, null);
                manager.discoverServices(channel, null);
            }


            channel.close();
        }

        serviceInfo = null;
        channel = null;
        System.gc();

        if (context != null) {
            if (manager == null) {
                manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
            }

            if (channel == null) {
                channel = manager.initialize(context, context.getMainLooper(), null);
            }
        }
    }

    public void startRegistration() {
        //  Create a string map containing information about your service.
        Map record = new HashMap();
        record.put("listenport", String.valueOf(getPort()));
        record.put("buddyname", "John Doe" + (int) (Math.random() * 1000));
        record.put("available", "visible");

        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.

        final String androidSecureID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        final String serviceName = androidSecureID + "-" + MultipleUse.getApplicationName(context).replace(" ", "-") + "-" + context.getString(R.string.infected_tag).toUpperCase();

        serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance(serviceName, "_presence._tcp", record);

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.


        manager.addLocalService(channel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.v(BuildConfig.APPLICATION_ID, "Started service: " + serviceName);
                // Command successful! Code isn't necessarily needed here,
                // Unless you want to update the UI or add logging statements.
            }

            @Override
            public void onFailure(int arg0) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
            }
        });
    }


}
