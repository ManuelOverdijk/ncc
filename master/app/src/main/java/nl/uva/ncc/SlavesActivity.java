package nl.uva.ncc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;

import se.bitcraze.crazyfliecontrol.R;
import com.example.mymodule.app2.Slave;

public class SlavesActivity extends Activity implements PeerListListener, SlaveLocationListener {
    ListView mListView;
    Button mButton;
    Button mButtonVisualize;
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
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
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
                Toast.makeText(getApplicationContext(), "Discovering..", Toast.LENGTH_LONG).show();
            }
        });

        mButtonVisualize = (Button) findViewById(R.id.button_visualize);
        mButtonVisualize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> mSlavesToString = new ArrayList<String>();
                ArrayList<String> mSlavesLatitude = new ArrayList<String>();
                ArrayList<String> mSlavesLongitude = new ArrayList<String>();
                for(Slave slave : mSlaves) {
                    if(slave.getName() == null || slave.getName().length() == 0) {
                        mSlavesToString.add(slave.getIdentifier());
                    } else {
                        mSlavesToString.add(slave.getName());
                    }
                    mSlavesLatitude.add(Double.toString(slave.getLatitude()));
                    mSlavesLongitude.add(Double.toString(slave.getLongitude()));
                }

                Intent intent = new Intent(getApplicationContext(), SlavesSimulate.class);
                intent.putStringArrayListExtra("slavesnames", mSlavesToString);
                intent.putStringArrayListExtra("slaveslat", mSlavesLatitude);
                intent.putStringArrayListExtra("slaveslon", mSlavesLongitude);
                startActivity(intent);
            }
        });

        ServerTask.setServerTaskListener(this);
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
    }

    @Override
    public void onLocationReceived(Slave receivedSlave) {
        int index = mSlaves.indexOf(receivedSlave);

        if (index == -1) {
            Log.e("", "Received location from slave not known in mSlaves");
            return;
        } else {
            mSlaves.set(index, receivedSlave);
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
        Collection<WifiP2pDevice> deviceList = wifiP2pDeviceList.getDeviceList();
        Log.d("", "Peers available called. Found peers: " + deviceList.size());

        // Remove slaves from mSlaves that aren't connected anymore
        for (Slave slave : mSlaves) {
            boolean isConnected = false;
            for (WifiP2pDevice device : wifiP2pDeviceList.getDeviceList()) {
                if (slave.getIdentifier().equals(Slave.getTrimmedMAC(device.deviceAddress))) {
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
                if (slave.getIdentifier().equals(Slave.getTrimmedMAC(device.deviceAddress))) {
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
                    mButtonVisualize.setEnabled(true);
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
