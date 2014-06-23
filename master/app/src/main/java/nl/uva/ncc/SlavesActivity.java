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

import uva.nc.ServiceActivity;
import uva.nc.bluetooth.BluetoothService;

public class SlavesActivity extends ServiceActivity {

    ListView mListView;
    ArrayAdapter mAdapter;
    ArrayList<Slave> mSlaves;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate=(savedInstanceState);
        setContentView(R.layout.activity_slaves);
    }

    @Override void onBluetoothReady(BluetoothService bluetooth) {

    }
}
