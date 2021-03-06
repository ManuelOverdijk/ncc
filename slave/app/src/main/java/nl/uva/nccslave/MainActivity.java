package nl.uva.nccslave;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymodule.app2.DevicePacket;
import com.example.mymodule.app2.DeviceType;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class MainActivity extends Activity implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener,
        SensorEventListener {

    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    // Location
    LocationRequest mLocationRequest;
    LocationClient mLocationClient;

    // Bearing
    private SensorManager sensorManager;
    float[] mGravity = null;
    float[] mGeomagnetic = null;
    float mBearing = 0;

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WiFiDirectBroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    // UI elements
    private TextView mTvLatitude;
    private TextView mTvLongitude;
    private EditText mNameInput;
    private Spinner mDeviceTypeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        // Setup location requests
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(
                LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationClient = new LocationClient(this, this, this);

        // setup wifi direct framework
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel);

        // register broadcastreceiver
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        registerReceiver(mReceiver, mIntentFilter);

        // get ui elements
        mTvLatitude = (TextView) findViewById(R.id.tvLatitude);
        mTvLongitude = (TextView) findViewById(R.id.tvLongitude);
        Button mButton = (Button) findViewById(R.id.button_discover);
        mNameInput = (EditText) findViewById(R.id.input_name);
        mDeviceTypeSpinner = (Spinner) findViewById(R.id.device_type_spinner);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mReceiver.startDiscovery();
                mDeviceTypeSpinner.setEnabled(false);
            }
        });

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.device_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mDeviceTypeSpinner.setAdapter(adapter);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    /* start location requests */
    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);

        // Register for sensors
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                sensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                sensorManager.SENSOR_DELAY_NORMAL);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);

        // Unregister for sensors
        sensorManager.unregisterListener(this);
    }

    /*
     * LOCATION
     */
    //TODO: doet op het moment helemaal niks..
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case CONNECTION_FAILURE_RESOLUTION_REQUEST :
            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK :
                    /*
                     * Try the request again
                     */
                        break;
                }
        }
    }

    //TODO: Wordt niet gebruikt..
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.
                        isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates",
                    "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else {
            // Get the error code
            showErrorDialog(resultCode);
            return false;
        }
    }

    private void showErrorDialog(int resultCode) {
        GooglePlayServicesUtil.getErrorDialog(
                resultCode,
                this,
                resultCode).show();
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Toast.makeText(this, "Connected to Google Play services", Toast.LENGTH_SHORT).show();
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected from Google Play services. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Show location in UI
        mTvLatitude.setText(Double.toString(location.getLatitude()));
        mTvLongitude.setText(Double.toString(location.getLongitude()));

        // Get own MAC address to send to master
        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        String address = info.getMacAddress();

        // Get own name
        String name = "";
        if (mNameInput.getText() != null && mNameInput.getText().length() != 0) {
            name = mNameInput.getText().toString();
        }

        // Get device type
        String deviceTypeStr = (String) mDeviceTypeSpinner.getSelectedItem();
        DeviceType deviceType = DevicePacket.stringToDeviceType(deviceTypeStr);

        // Create slave object to send to master
        DevicePacket devicePacket = new DevicePacket();
        devicePacket.setIdentifier(address);
        devicePacket.setName(name);
        devicePacket.setLatitude(location.getLatitude());
        devicePacket.setLongitude(location.getLongitude());
        devicePacket.setDeviceType(deviceType);
        devicePacket.setBearing(mBearing);

        // Send location to server
        new ClientTask().execute(devicePacket);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    // from http://stackoverflow.com/questions/15155985/android-compass-bearing
    // Gets bearing in degrees. 0/360 = north
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // Get gravity
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = sensorEvent.values;

        // Get magnetic field
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = sensorEvent.values;

        // Get bearing
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
                    mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimuthInRadians = orientation[0];
                float azimuthInDegrees = (float)(Math.toDegrees(azimuthInRadians)+360)%360;
                mBearing = azimuthInDegrees;
            }
        }
    }
}
