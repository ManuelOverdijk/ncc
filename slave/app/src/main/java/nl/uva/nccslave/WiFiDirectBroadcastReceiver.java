package nl.uva.nccslave;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

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

    public void syncStopDiscovery() {
        if (mManager == null || mChannel == null) {
            return;
        }

        final Semaphore semaphore = new Semaphore(1, true);

        try {
            semaphore.acquire();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                semaphore.release();
            }

            @Override
            public void onFailure(int reason) {
                semaphore.release();
            }
        });

        try {
            semaphore.acquire();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void syncDisconnect() {
        if (mManager == null || mChannel == null) {
            return;
        }

        final Semaphore semaphore = new Semaphore(1, true);

        try {
            semaphore.acquire();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                semaphore.release();
            }

            @Override
            public void onFailure(int reason) {
                semaphore.release();
            }
        });

        try {
            semaphore.acquire();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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
            Log.d("", "PEERS CHANGED action. Requesting list of peers.");
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
                        if (!info.groupFormed || !info.isGroupOwner) {
                            return;
                        }


                        // We are the owner of the group of devices, aka
                        // the master. Create a server thread and accept
                        // incoming connections.
                        Log.d("", "Group formed, group owner.");

                    }
                });
            }
        }
    }
}