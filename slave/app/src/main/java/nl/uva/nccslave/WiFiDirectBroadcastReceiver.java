package nl.uva.nccslave;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.util.Log;

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager mManager;
    private Channel mChannel;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager,
                                       Channel channel) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
    }

    // Start peer discovery
    public void startDiscovery() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("", "Discover availablePeers success");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d("", "Discover availablePeers failure. reason: " + reasonCode);
            }
        });
    }

    /*
     * Broadcast receiver logic / overrides
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("", "received broadcast: " + action);

        if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo != null && networkInfo.isConnected()) {
                // We are connected with the other device, request connection
                // info to find group owner IP.
                Log.d("", "Requesting connection info");
                mManager.requestConnectionInfo(mChannel, new ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        if (info.groupFormed && !info.isGroupOwner) {
                            ClientTask.setGroupOwnerAddress(info.groupOwnerAddress);
                        } else if (info.groupFormed) {
                            Log.e("", "Slave shouldn't be group owner. Closing application now");
                            System.exit(0);
                        }
                    }
                });
            }
        }
    }
}