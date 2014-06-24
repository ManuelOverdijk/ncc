package nl.uva.ncc;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.AsyncTask;

/**
 * Created by datwelk on 23/06/14.
 */
public class ServerTask extends AsyncTask<Void, Void, Void>{
    public static Parcel unmarshall(byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0); // this is extremely important!
        return parcel;
    }

    public static <T> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
        Parcel parcel = unmarshall(bytes);
        return creator.createFromParcel(parcel);
    }

    @Override
    protected Void doInBackground(Void... params) {
        ServerSocket serverSocket;

        try {
            serverSocket = new ServerSocket(8888);
        } catch (IOException e) {
            Log.e("LocationServerAsyncTask", e.getMessage());
            return null;
        }

        while(true) {
            try {
                Log.d("", "before accept");
                Socket client = serverSocket.accept();
                Log.d("", "" + client.getRemoteSocketAddress().toString());
                Log.d("", client.getInetAddress().getHostAddress());
                Log.d("", "after accept");

                client.getInetAddress().getHostad

                InputStream inputstream = client.getInputStream();

                Log.d("LocationServerAsyncTask", "Received from client");

                byte[] receivedBytes = new byte[128];
                inputstream.read(receivedBytes, 0, 128);

                Location location = unmarshall(receivedBytes, Location.CREATOR);
                Log.d("", "received location, long: " + location.getLongitude() + " lat: " + location.getLatitude());
            } catch (IOException e) {
                Log.e("LocationServerAsyncTask", e.getMessage());
                return null;
            }
        }
    }
}