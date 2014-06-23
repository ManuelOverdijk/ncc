package nl.uva.ncc;

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

                Log.d("LocationServerAsyncTask", "Received from client");

                int result = inputstream.read();

                Log.d("", "received int: " +  result);
            } catch (IOException e) {
                Log.e("LocationServerAsyncTask", e.getMessage());
                return null;
            }
        }
    }
}