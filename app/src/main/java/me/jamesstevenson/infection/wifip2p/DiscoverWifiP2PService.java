package me.jamesstevenson.infection.wifip2p;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;

import java.util.Map;

import me.jamesstevenson.infection.BuildConfig;
import me.jamesstevenson.infection.R;
import me.jamesstevenson.infection.utils.InfectedStateManager;

/**
 * This class utilises WiFi P2P Network Service discovery to find nearby devices broadcasting network services.
 */
public class DiscoverWifiP2PService {

    WifiP2pManager.Channel channel;
    WifiP2pManager manager;
    WifiP2pDnsSdServiceRequest serviceRequest;
    String TAG = this.getClass().getSimpleName();
    Context context;

    /**
     * Constructor
     *
     * @param context
     */
    public DiscoverWifiP2PService(Context context) {
        this.manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        this.channel = manager.initialize(context, context.getMainLooper(), null);
        this.context = context;
    }

    /**
     * Starts the discovery of network services
     */
    public void discovery() {
        manager.discoverServices(channel, null);
    }

    /**
     * Initialises the required callbacks to use wifi p2p
     */
    public void setupDiscovery() {
        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override

            public void onDnsSdTxtRecordAvailable(
                    String fullDomain, Map record, WifiP2pDevice device) {
            }
        };

        // Triggers when a nearby wifi p2p network service is found
        WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                WifiP2pDevice resourceType) {


                if (BuildConfig.DEBUG) {
                    Log.v(TAG, "Device name " + resourceType.deviceName);
                    Log.v(TAG, "Instance Name " + instanceName);
                }

                // Checks the nearby service and if it ends with INFECTED we set ourselves to the infected state.
                // Here there is the edge case that there is another non-related wifip2p service called infected
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
                Log.e(TAG, "P2P isn't supported on this device.");
            }
        });
    }
}
