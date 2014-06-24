package nl.uva.ncc;

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
    private PeerListListener mListener;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager,
                                       Channel channel,
                                       PeerListListener listener) {
        super();

        this.mManager = manager;
        this.mChannel = channel;
        this.mListener = listener;
    }

    /*
     * Connect / disconnect
     */
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

        if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // The number of peers around us has changed. Request
            // a list of all peers. mPeerListListener will receive
            // this list on completion of all peers and will try
            // to connect to each peer in the list.
            Log.d("", "Requesting list of peers.");
            mManager.requestPeers(mChannel, mListener);
        }

        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d("", "P2P CONNECTION CHANGED action. Connected or disconnected to peer.");

            // Respond to new connection or disconnections
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo != null && networkInfo.isConnected()) {
                // We are connected with the other device, request connection
                // info to find group owner IP.
                Log.d("", "requesting connection info");
                mManager.requestConnectionInfo(mChannel, new ConnectionInfoListener() {
                    @Override
                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        if (info.groupFormed && info.isGroupOwner) {
                            // We are the owner of the group of devices, aka
                            // the master. Create a server thread and accept
                            // incoming connections.
                            Log.d("", "Group formed, group owner.");

                            new ServerTask().execute();
                        } else if (info.groupFormed) {
                            Log.e("", "Master should be group owner. Closing application now");
                            System.exit(0);
                        }
                    }
                });
            }
        }
    }
}