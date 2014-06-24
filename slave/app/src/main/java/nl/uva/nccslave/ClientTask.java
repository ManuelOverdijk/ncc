package nl.uva.nccslave;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Sends GPS location to server in an asynchronous task. Should be called every time the
 * location changes. groupOwnerAddress should be set first.
 */
public class ClientTask extends AsyncTask<Location, Void, Void> implements Serializable {
    private static InetAddress groupOwnerAddress;

    public static void setGroupOwnerAddress(InetAddress address) {
        groupOwnerAddress = address;
    }

    // Serializes an object (Location) into a byte array.
    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        Log.d("", "size: " + out.size());
        return out.toByteArray();
    }

    @Override
    protected Void doInBackground(Location... location) {
        if (groupOwnerAddress == null) {
            Log.d("", "groupOwnerAddress not set");
            return null;
        }

        if (location == null) {
            Log.d("", "No location provided to send.");
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
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(location);
            objectOutputStream.close();

            Log.d("", "Transmitted location");

        } catch (FileNotFoundException e) {
            Log.e("", e.toString());
        } catch (IOException e) {
            Log.e("", e.toString());
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
                    Log.e("", e.toString());
                }
            }
        }

        return null;
    }
}