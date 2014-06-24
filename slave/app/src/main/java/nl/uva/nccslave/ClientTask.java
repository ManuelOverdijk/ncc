package nl.uva.nccslave;

import android.location.Location;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
public class ClientTask extends AsyncTask<Slave, Void, Void> implements Serializable {
    private static InetAddress groupOwnerAddress;

    public static void setGroupOwnerAddress(InetAddress address) {
        groupOwnerAddress = address;
    }

    // Serializes an object (Location) into a byte array.
    public static byte[] marshall(Parcelable parceable) {
        Parcel parcel = Parcel.obtain();
        parceable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle(); // not sure if needed or a good idea
        return bytes;
    }

    @Override
    protected Void doInBackground(Slave... slaves) {
        Slave slave = slaves[0];
        if (groupOwnerAddress == null) {
            Log.d("", "groupOwnerAddress not set");
            return null;
        }

        if (slave == null) {
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

            // Send Location
            byte[] output = marshall(slave);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(output);
            outputStream.close();
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