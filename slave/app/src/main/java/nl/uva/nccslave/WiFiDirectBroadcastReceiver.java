package nl.uva.nccslave;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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

    // Listener that gives access to available peers to connect to.
    void setupPeerListener() {
        mPeerListListener = new PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                // store list for later use
                mAvailablePeers.clear();
                mAvailablePeers.addAll(wifiP2pDeviceList.getDeviceList());

                Log.d("", "number of found peers: " + mAvailablePeers.size());

                // Try to connect to all available peers
                for (final WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
                    // config is needed to connect. groupOwnerIntent tells the inclination
                    // to be the group owner. 0 means least inclination.
                    WifiP2pConfig config = new WifiP2pConfig();
                    config.deviceAddress = device.deviceAddress;
                    config.groupOwnerIntent = 0;

                    mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            //success logic
                            Log.d("", "Connection established");
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

    // Listener that provides connection information
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
                    Log.e("", "Client shouldn't be a group owner. Disconnecting now.");
                    disconnect();
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

    // Start peer discovery
    void connect() {
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

    // Disconnect from group
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
                                Log.d("", "removeGroup onSuccess -");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d("", "removeGroup onFailure -" + reason);
                            }
                        });
                    }
                }
            });
        }
    }

    // Broadcast receiver
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d("", "received broadcast: " + action);

        if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {
                mManager.requestPeers(mChannel, mPeerListListener);
            }
        }

        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Respond to new connection or disconnections
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo != null && networkInfo.isConnected()) {
                // We are connected with the other device, request connection
                // info to find group owner IP
                Log.d("", "requesting connection info");
                mManager.requestConnectionInfo(mChannel, mConnectionInfoListener);
            }
        }
    }

    /**
     * Sends GPS location to server in an asynchronous task. Should be called every time the
     * location changes. groupOwnerAddress should be set first.
     */
    public static class LocationClientAsyncTask extends AsyncTask<Location, Void, Void> implements Serializable{
        private static InetAddress groupOwnerAddress;

        public static void setGroupOwnerAddress(InetAddress address) {
            groupOwnerAddress = address;
        }

        // Serializes an object (Location) into a byte array.
        public static byte[] serialize(Object obj) throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(obj);
            return out.toByteArray();
        }

        @Override
        protected Void doInBackground(Location... location) {
            if (groupOwnerAddress == null) {
                Log.d("", "groupOwnerAddress not set");
                return null;
            }

            if (location == null) {
                Log.d("", "No location provided to send.");
                return null;
            }

            Socket socket = new Socket();

            try {
                /**
                 * Create a client socket with the host,
                 * port, and timeout information.
                 */
                socket.bind(null);

                Log.d("", "Socket trying to connect to: " + groupOwnerAddress);
                socket.connect(new InetSocketAddress(groupOwnerAddress, 8888), 500);
                Log.d("", "Connect call returned");

                // Send Location
                byte[] output = serialize(location);
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(output);
                outputStream.close();
            } catch (FileNotFoundException e) {
                Log.e("", e.toString());
            } catch (IOException e) {
                Log.e("", e.toString());
            }

            /**
             * Clean up any open sockets when done
             * transferring or if an exception occurred.
             */
            finally {
                if (socket.isConnected()) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        Log.e("", e.toString());
                    }
                }
            }

            return null;
        }
    }
}
