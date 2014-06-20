package com.example.projectdroneserver.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.projectdroneserver.app.R;

public class MainActivity extends Activity {
    GPSLocationProvider gpsLocationProvider;

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WiFiDirectBroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpsLocationProvider = new GPSLocationProvider(this);

        setupButtons();

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }
    /* unregister the broadcast receiver */
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

    void setupButtons() {
        Button buttonGPS = (Button)findViewById(R.id.button_show);
        buttonGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLocation();
                mReceiver.discoverPeers();
            }
        });

        Button buttonStart = (Button)findViewById(R.id.button_start);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gpsLocationProvider.startMeasurements();
                new WiFiDirectBroadcastReceiver.LocationClientAsyncTask().execute();
            }
        });

        Button buttonStop = (Button)findViewById(R.id.button_stop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gpsLocationProvider.stopMeasurements();
                mReceiver.disconnect();
            }
        });

        Button buttonClear = (Button)findViewById(R.id.button_reset);
        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gpsLocationProvider.resetMeasurements();
            }
        });
    }

    void showLocation() {
        Location location = gpsLocationProvider.getLocation();
        Toast.makeText(
                getApplicationContext(),
                "Latitude: " + location.getLatitude() +
                "\nLongitude: " + location.getLongitude(),
                Toast.LENGTH_SHORT).show();
    }
}
