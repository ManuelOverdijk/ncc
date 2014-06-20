package com.example.projectdroneserver.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.AsyncTask;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
    private WifiP2pManager.ConnectionInfoListener mConnectionInfoListener;

    private ArrayList<WifiP2pDevice> availablePeers;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel) {
        super();
        this.mManager = manager;
        this.mChannel = channel;

        availablePeers = new ArrayList<WifiP2pDevice>();

        setupPeerListener();
        setupConnectionInfoListener();
    }

    // Gives access to available peers
    void setupPeerListener() {
        mPeerListListener = new PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                availablePeers.clear();
                availablePeers.addAll(wifiP2pDeviceList.getDeviceList());

                Log.d("abc", "number of found peers: " + availablePeers.size());

                for (final WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
                    WifiP2pConfig config = new WifiP2pConfig();
                    config.deviceAddress = device.deviceAddress;
                    // TODO: 0 kleinste kans op GO, 15 grootste kans, -1 Doe maar wat
                    config.groupOwnerIntent = 15;
                    mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                            //success logic
                            Log.d("", "Connection established");
                            //TODO: misschien loopt ie omdat device nu uit available peers verdwijnt?
                        }

                        @Override
                        public void onFailure(int reason) {
                            //failure logic
                            Log.d("", "Connection failed. reason: " + reason);
                        }
                    });
                }
            }
        };
    }

    // Returns group info
    void setupConnectionInfoListener() {
        mConnectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(final WifiP2pInfo info) {

                // After the group negotiation, we can determine the group owner.
                if (info.groupFormed && info.isGroupOwner) {
                    // Do whatever tasks are specific to the group owner.
                    // One common case is creating a server thread and accepting
                    // incoming connections.
                    Log.d("", "Group formed, group owner");
                    new LocationServerAsyncTask().execute();
                } else if (info.groupFormed) {
                    // The other device acts as the client. In this case,
                    // you'll want to create a client thread that connects to the group
                    // owner.
                    Log.d("", "Group formed, group client");
                    LocationClientAsyncTask.setGroupOwnerAddress(info.groupOwnerAddress);
                }
            }
        };
    }

    // Broadcast receiver
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("abc", "received broadcast: " + action);

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled

//                if (mManager != null) {
//                    mManager.requestPeers(mChannel, mPeerListListener);
//                }
            } else {
                // Wi-Fi P2P is not enabled
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current availablePeers
//            int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
//            Log.d("abc", "p2p peers changed action. discovery state: " + state);
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            WifiP2pDevice device = (WifiP2pDevice) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);

            if (networkInfo.isConnected()) {

                // We are connected with the other device, request connection
                // info to find group owner IP
                Log.d("abc", "requesting conncetion info");
                mManager.requestConnectionInfo(mChannel, mConnectionInfoListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }

    // Wait for incoming gps information. call once, it has a loop
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
                    Log.d("abc", "before accpet");
                    Socket client = serverSocket.accept();
                    Log.d("abc", "after accept");

                    InputStream inputstream = client.getInputStream();

                    Log.d("LocationServerAsyncTask", "Received from client");

                    int result = inputstream.read();

                    Log.d("abc", "received int: " +  result);
                } catch (IOException e) {
                    Log.e("LocationServerAsyncTask", e.getMessage());
                    return null;
                }
            }
        }
    }

    // Push gps information to master. call everytime gps location has changed.
    // groupOwnerAddress should be set first.
    public static class LocationClientAsyncTask extends AsyncTask<Void, Void, Void> {
        private static InetAddress groupOwnerAddress;

        public static void setGroupOwnerAddress(InetAddress address) {
            groupOwnerAddress = address;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (groupOwnerAddress == null) {
                Log.d("abc", "groupOwnerAddress not set");
                return null;
            }

            Socket socket = new Socket();

            try {
                /**
                 * Create a client socket with the host,
                 * port, and timeout information.
                 */
                socket.bind(null);
                Log.d("abc", "before connect. Groupowneraddress: " + groupOwnerAddress);
                socket.connect(new InetSocketAddress(groupOwnerAddress, 8888), 500);
                Log.d("abc", "after connect");

                /**
                 * Create a byte stream from a JPEG file and pipe it to the output stream
                 * of the socket. This data will be retrieved by the server device.
                 */
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(69);
                outputStream.close();
            } catch (FileNotFoundException e) {
                //catch logic
            } catch (IOException e) {
                //catch logic
            }

            /**
             * Clean up any open sockets when done
             * transferring or if an exception occurred.
             */ finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            //catch logic
                        }
                    }
                }
            }
            return null;
        }
    }

    // Start peer discovery
    void discoverPeers() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d("", "Discover availablePeers success");
                if (mManager != null) {
                    mManager.requestPeers(mChannel, mPeerListListener);
                }
            }

            @Override
            public void onFailure(int reasonCode) {
                Log.d("", "Discover availablePeers failure. reason: " + reasonCode);
            }
        });
    }

    public void disconnect() {
        if (mManager != null && mChannel != null) {
            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && mManager != null && mChannel != null
                            && group.isGroupOwner()) {
                        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d("abc", "removeGroup onSuccess -");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d("abc", "removeGroup onFailure -" + reason);
                            }
                        });
                    }
                }
            });
        }
    }
}