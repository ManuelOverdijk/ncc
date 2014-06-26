package nl.uva.ncc;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.AsyncTask;
import com.example.mymodule.app2.DevicePacket;

/**
 * Created by datwelk on 23/06/14.
 */
public class ServerTask extends AsyncTask<Void, DevicePacket, Void> {
    private static SlaveLocationListener mSlaveLocationListener;

    public static void setmSlaveLocationListener(SlaveLocationListener slaveLocationListener) {
        mSlaveLocationListener = slaveLocationListener;
    }

    public static DevicePacket deserialize(InputStream inputStream) {
        DevicePacket devicePacket = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            devicePacket = (DevicePacket)objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return devicePacket;
    }

    @Override
    protected void onProgressUpdate(DevicePacket... devicePackets) {
        DevicePacket receivedDevicePacket = devicePackets[0];
        mSlaveLocationListener.onLocationReceived(receivedDevicePacket);
    }

    @Override
    protected Void doInBackground(Void... params) {
        ServerSocket serverSocket;

        try {
            serverSocket = new ServerSocket(8888);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        while(true) {
            try {
                Log.d("", "before accept");
                Socket client = serverSocket.accept();
                Log.d("", "after accept");

                DevicePacket receivedDevicePacket = deserialize(client.getInputStream());
                publishProgress(receivedDevicePacket);

                Log.d("", "Received location from device: " + receivedDevicePacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}