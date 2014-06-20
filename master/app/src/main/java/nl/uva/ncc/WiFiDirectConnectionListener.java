package nl.uva.ncc;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * Created by datwelk on 20/06/14.
 */
public interface WiFiDirectConnectionListener {
    public void onDeviceConnected(WifiP2pDevice device);
    public void onDeviceDisconnected(WifiP2pDevice device);
}
