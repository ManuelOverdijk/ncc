package nl.uva.ncc;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import se.bitcraze.crazyfliecontrol.R;

public class SlavesActivity extends Activity {

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

        initWiFiDirectBroadcastReceiver();

        mSlaves = new ArrayList<Slave>();
        mAdapter = new SlaveAdapter(this, R.layout.view_slave_item, mSlaves);
        mListView = (ListView)findViewById(R.id.listView);
        mListView.setAdapter(mAdapter);
        mButton = (Button)findViewById(R.id.button_discover);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: disconnect
                mReceiver.connect();
            }
        });
    }

    void initWiFiDirectBroadcastReceiver() {
        // Properly clean up old receiver
        if (mReceiver != null) {
            mReceiver.disconnect();
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }

        // Init new receiver
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel);

        // Register for broadcasts
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        registerReceiver(mReceiver, mIntentFilter);

        mReceiver.setOnConnectionChangedListener(new WiFiDirectConnectionListener() {
            @Override
            public void onDeviceConnected(WifiP2pDevice device) {
                Slave slave = new Slave();
                slave.setIdentifier(device.deviceAddress);
                mSlaves.add(slave);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onDevicesDisconnected() {
                /* Old code: */
//                Slave slave = null;
//
//                for (Slave aSlave : mSlaves) {
//                    if (slave.getIdentifier().equals(device.deviceAddress)) {
//                        slave = aSlave;
//                    }
//                }
//
//                if (slave != null) {
//                    mSlaves.remove(slave);
//                    mAdapter.notifyDataSetChanged();
//                }

                mSlaves.clear();

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
        mReceiver.disconnect();
    }
}
