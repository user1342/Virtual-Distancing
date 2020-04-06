package com.example.infection.wifip2p;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;

import com.example.infection.R;
import com.example.infection.utils.InfectedStateManager;

import java.util.Map;

public class DiscoverWifiP2PService {

    WifiP2pManager.Channel channel;
    WifiP2pManager manager;
    WifiP2pDnsSdServiceRequest serviceRequest;
    String TAG = this.getClass().getSimpleName();
    Context context;

    public DiscoverWifiP2PService(Context context) {
        this.manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        this.channel = manager.initialize(context, context.getMainLooper(), null);
        this.context = context;
    }

    public void discovery() {
        manager.discoverServices(channel, null);
    }

    public void setupDiscovery() {
        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            /* Callback includes:
             * fullDomain: full domain name: e.g "printer._ipp._tcp.local."
             * record: TXT record dta as a map of key/value pairs.
             * device: The device running the advertised service.
             */

            public void onDnsSdTxtRecordAvailable(
                    String fullDomain, Map record, WifiP2pDevice device) {
                //Log.d(TAG, "DnsSdTxtRecord available -" + record.toString());
                //buddies.put(device.deviceAddress, record.get("buddyname"));
            }
        };


        WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                WifiP2pDevice resourceType) {

                // Update the device name with the human-friendly version from
                // the DnsTxtRecord, assuming one arrived.

                Log.d(TAG, "Device name " + resourceType.deviceName);
                Log.d(TAG, "Instance Name " + instanceName);

                // Checks the nearby service and if it ends with INFECTED we set ourselves to the infected state.
                // Here there is the edge case that there is anouther non-related wifip2p service called infected
                // which will trigger a state change.
                if (instanceName.endsWith(context.getString(R.string.infected_tag).toUpperCase())) {
                    InfectedStateManager infectedStateManager;
                    infectedStateManager = new InfectedStateManager(context);
                    infectedStateManager.setNewState(context.getString(R.string.infected_tag), false);
                }

            }
        };

        manager.setDnsSdResponseListeners(channel, servListener, txtListener);


        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        manager.addServiceRequest(channel,
                serviceRequest,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // Success!
                    }

                    @Override
                    public void onFailure(int code) {
                        // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                    }
                });


        manager.discoverServices(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // Success!
            }

            @Override
            public void onFailure(int code) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                Log.d(TAG, "P2P isn't supported on this device.");

            }
        });
    }
}
