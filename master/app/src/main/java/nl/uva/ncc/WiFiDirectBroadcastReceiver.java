package nl.uva.ncc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
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

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager mManager;
    private Channel mChannel;

    private PeerListListener mPeerListListener;
    private ConnectionInfoListener mConnectionInfoListener;
    private WiFiDirectConnectionListener mListener;

    private ArrayList<WifiP2pDevice> mAvailablePeers;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel) {
        super();
        this.mManager = manager;
        this.mChannel = channel;

        // Initialize variables
        mAvailablePeers = new ArrayList<WifiP2pDevice>();

        // Setup Listeners
        setupPeerListener();
        setupConnectionInfoListener();
    }

    /**
     * setters
     * @param listener
     */
    public void setOnConnectionChangedListener(WiFiDirectConnectionListener listener) {
        this.mListener = listener;
    }

    // Listener that gives access to available peers to connect to.
    void setupPeerListener() {
        mPeerListListener = new PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                mAvailablePeers.clear();
                mAvailablePeers.addAll(wifiP2pDeviceList.getDeviceList());

                Log.d("", "Peers available called. Found peers: " + mAvailablePeers.size());

                // Connect to each device that is available.
                for (final WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
                    // config is needed to connect. groupOwnerIntent tells the inclination
                    // to be the group owner. 0 means least inclination.
                    WifiP2pConfig config = new WifiP2pConfig();
                    config.deviceAddress = device.deviceAddress;
                    config.groupOwnerIntent = 15;

                    mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            // Successfully connected to this device.
                            Log.d("", "Connection established");
                            if (mListener != null) {
                                mListener.onDeviceConnected(device);
                            }
                        }

                        @Override
                        public void onFailure(int reason) {
                            // Failed to connect to this device.
                            Log.d("", "Connection failed. reason: " + reason);
                        }
                    });
                }
            }
        };
    }

    // Listener that provides connection information
    void setupConnectionInfoListener() {
        mConnectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(final WifiP2pInfo info) {

                if (!info.groupFormed || !info.isGroupOwner) {
                    return;
                }

                // We are the owner of the group of devices, aka
                // the master. Create a server thread and accept
                // incoming connections.
                Log.d("", "Group formed, group owner.");

                new LocationServerAsyncTask().execute();
            }
        };
    }

    /*
     * Connect / disconnect
     */
    public void connect() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("", "Discover availablePeers success");
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d("", "Discover availablePeers failure. reason: " + reasonCode);
//                mSlavesActivity.initWiFiDirectBroadcastReceiver();
            }
        });
    }

    public void disconnect() {
        // This code is not synchronous... Might
        // need to change that in the future.
        if (mManager == null || mChannel == null) {
            return;
        }

        mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                if (group == null ||
                        mManager == null ||
                        mChannel == null ||
                        !group.isGroupOwner()) {
                    return;
                }

                mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d("abc", "removeGroup onSuccess");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d("", "removeGroup onFailure -" + reason);
                    }
                });
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
            // reset connected peers
            if (mListener != null) {
                mListener.onDevicesDisconnected();
            }
            // The number of peers around us has changed. Request
            // a list of all peers. mPeerListListener will receive
            // this list on completion of all peers and will try
            // to connect to each peer in the list.
            Log.d("", "PEERS CHANGED action. Requesting list of peers.");
            mManager.requestPeers(mChannel, mPeerListListener);
        }

        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Log.d("", "P2P CONNECTION CHANGED action. Connected or disconnected to peer.");

            // Respond to new connection or disconnections
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo != null && networkInfo.isConnected()) {
                // We are connected with the other device, request connection
                // info to find group owner IP.
                Log.d("", "requesting connection info");
                mManager.requestConnectionInfo(mChannel,
                        mConnectionInfoListener);
            }
        }
    }

    /*
     * Server thread / task. Handle incoming connections
     * and data.
     */
    public static class LocationServerAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ServerSocket serverSocket;

            try {
                serverSocket = new ServerSocket(8888);
            } catch (IOException e) {
                Log.e("LocationServerAsyncTask", e.getMessage());
                return null;
            }

            while(true) {
                try {
                    Log.d("", "before accept");
                    Socket client = serverSocket.accept();
                    Log.d("", "after accept");

                    InputStream inputstream = client.getInputStream();

                    Log.d("LocationServerAsyncTask", "Received from client");

                    int result = inputstream.read();

                    Log.d("", "received int: " +  result);
                } catch (IOException e) {
                    Log.e("LocationServerAsyncTask", e.getMessage());
                    return null;
                }
            }
        }
    }
}