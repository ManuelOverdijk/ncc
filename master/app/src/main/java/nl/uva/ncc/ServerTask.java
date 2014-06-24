package nl.uva.ncc;

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
public class ServerTask extends AsyncTask<Void, Slave, Void> {
    private static ServerTaskListener mServerTaskListener;

    public static void setServerTaskListener(ServerTaskListener serverTaskListener) {
        mServerTaskListener = serverTaskListener;
    }

    public static Slave deserialize(InputStream inputStream) {
        Slave slave = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            slave = (Slave)objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return slave;
    }

    @Override
    protected void onProgressUpdate(Slave... slaves) {
        Slave receivedSlave = slaves[0];
        mServerTaskListener.onLocationReceived(receivedSlave);

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

                Slave receivedSlave = deserialize(client.getInputStream());
                publishProgress(receivedSlave);

                Log.d("", "Received location from device: " + receivedSlave);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}