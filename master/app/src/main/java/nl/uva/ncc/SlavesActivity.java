package nl.uva.ncc;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;

import se.bitcraze.crazyfliecontrol.R;

public class SlavesActivity extends Activity implements WifiP2pManager.PeerListListener {

    ListView mListView;
    Button mButton;
    ArrayAdapter mAdapter;
    ArrayList<Slave> mSlaves;

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WiFiDirectBroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slaves);

        // WiFi Direct stuff
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        registerReceiver(mReceiver, mIntentFilter);

        // UI stuff
        mSlaves = new ArrayList<Slave>();
        mAdapter = new SlaveAdapter(this, R.layout.view_slave_item, mSlaves);
        mListView = (ListView)findViewById(R.id.listView);
        mListView.setAdapter(mAdapter);
        mButton = (Button)findViewById(R.id.button_discover);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mReceiver.startDiscovery();
            }
        });
    }

    /*
     * Overrides
     */

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mReceiver.syncStopDiscovery();
        mReceiver.syncDisconnect();
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        Collection<WifiP2pDevice> deviceList = wifiP2pDeviceList.getDeviceList();
        Log.d("", "Peers available called. Found peers: " + deviceList.size());

        // Remove slaves from mSlaves that aren't connected anymore
        for (Slave slave : mSlaves) {
            boolean isConnected = false;
            for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
                if (slave.getIdentifier().equals(device.deviceAddress)) {
                    isConnected = true;
                    break;
                }
            }

            if (!isConnected) {
                mSlaves.remove(slave);
                mAdapter.notifyDataSetChanged();
            }
        }

        // Connect to each device that is available.
        for (final WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
            // config is needed to connect. groupOwnerIntent tells the inclination
            // to be the group owner. 0 means least inclination.
            boolean alreadyConnected = false;

            for (Slave slave : mSlaves) {
                if (slave.getIdentifier().equals(device.deviceAddress)) {
                    Log.d("", "Already connected to device.");
                    alreadyConnected = true;
                }
            }

            if (alreadyConnected) {
                continue;
            }

            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;
            config.groupOwnerIntent = 15; // we want to be group owner since we are master

            mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    // Successfully connected to this device.
                    // Request info about device
                    Log.d("", "Successfully connected to device");

                    Slave slave = new Slave();
                    slave.setIdentifier(device.deviceAddress);
                    mSlaves.add(slave);
                    mAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(int reason) {
                    // Failed to connect to this device.
                    Log.d("", "Connection failed. reason: " + reason);
                }
            });
        }
    }
}
