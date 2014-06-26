package nl.uva.nccslave;

import android.os.AsyncTask;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import com.example.mymodule.app2.DevicePacket;

/**
 * Sends GPS location to server in an asynchronous task. Should be called every time the
 * location changes. groupOwnerAddress should be set first.
 */
public class DroneAsyncTask extends AsyncTask<DevicePacket, Void, Void> implements Serializable {
    private static InetAddress groupOwnerAddress;

    public static void setGroupOwnerAddress(InetAddress address) {
        groupOwnerAddress = address;
    }

    @Override
    protected Void doInBackground(DevicePacket... devicePackets) {
        DevicePacket devicePacket = devicePackets[0];
        if (groupOwnerAddress == null) {
            Log.d("", "groupOwnerAddress not set");
            return null;
        }

        if (devicePacket == null) {
            Log.d("", "No slave provided to send.");
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

            OutputStream outputStream = socket.getOutputStream();

            // Serialize slave object and write to output stream.
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(devicePacket);
            objectOutputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}