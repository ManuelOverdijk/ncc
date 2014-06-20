package nl.uva.ncc;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import se.bitcraze.crazyfliecontrol.R;

public class SlavesActivity extends Activity {

    ListView mListView;
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

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mSlaves = new ArrayList<Slave>();
        mAdapter = new SlaveAdapter(this, R.layout.view_slave_item, mSlaves);
        mListView = (ListView)findViewById(R.id.listView);
        mListView.setAdapter(mAdapter);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel);
        mReceiver.setOnConnectionChangedListener(new WiFiDirectConnectionListener() {
            @Override
            public void onDeviceConnected(WifiP2pDevice device) {
                Slave slave = new Slave();
                slave.setIdentifier(device.deviceAddress);
                mSlaves.add(slave);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onDeviceDisconnected(WifiP2pDevice device) {
                Slave slave = null;

                for (Slave aSlave : mSlaves) {
                    if (slave.getIdentifier().equals(device.deviceAddress)) {
                        slave = aSlave;
                    }
                }

                if (slave != null) {
                    mSlaves.remove(slave);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        // Fixes a bug the Android guide introduced
        registerReceiver(mReceiver, mIntentFilter);
        mReceiver.connect();
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
        mReceiver.disconnect();
    }
}
