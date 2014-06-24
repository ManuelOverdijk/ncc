package nl.uva.ncc;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.AsyncTask;
import com.example.mymodule.app2.Slave;

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
                Log.d("", "after accept");

                InputStream inputstream = client.getInputStream();
                ObjectInputStream objectInputStream = new ObjectInputStream(inputstream);
                Slave slave = (Slave)objectInputStream.readObject();

                Log.d("", "Received lat: " + String.valueOf(slave.getLatitude()) + " lon: " + String.valueOf(slave.getLongitude()));
                Log.d("", "Received from device: " + slave.getIdentifier());
            } catch (IOException e) {
                Log.e("LocationServerAsyncTask", e.getMessage());
                return null;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}