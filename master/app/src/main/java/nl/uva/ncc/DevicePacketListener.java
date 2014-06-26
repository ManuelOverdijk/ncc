package nl.uva.ncc;

import com.example.mymodule.app2.DevicePacket;

/**
 * Created by datwelk on 24/06/14.
 */
public interface DevicePacketListener {
    public void onDevicePacketReceived(DevicePacket receivedDevicePacket);
}
