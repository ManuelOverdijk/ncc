package nl.uva.ncc;

/**
 * Created by koen on 24-6-14.
 */
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Random;

import se.bitcraze.crazyfliecontrol.R;

import static java.lang.Math.abs;

public class SlavesSimulate extends Activity {

    ListView mListView;
    ArrayAdapter mAdapter;
    Arrow arrow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_slaves_simulate);

        visualize_devices();
//        arrow = new Arrow(this);
//        setContentView(arrow);
//
//        arrow.setOnClickListener(new ImageView.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                arrow.setSimulation(arrow.getDirection() + 10, arrow.getThrust() + 10);
//            }
//        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        updateSizeInfo();
    }

    public void updateSizeInfo() {

    }

    private void visualize_devices() {
        Intent intent = getIntent();
        // get the names and coordinates of connected devices
        ArrayList<String> mNames = intent.getStringArrayListExtra("slavesnames");
        ArrayList<String> mSlavesLat = intent.getStringArrayListExtra("slaveslat");
        ArrayList<String> mSlavesLon = intent.getStringArrayListExtra("slaveslon");
        int nSlaves = mNames.size();

        // add new textview with info to layout for every device
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relative_layout);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int canvasWidth = size.x;
        int canvasHeight = size.y;

        if (nSlaves == 0) {
            return;
        }

        else if (nSlaves == 1) {
            TextView deviceTextview = new TextView(this);
            deviceTextview.setText(mNames.get(0) + " " + mSlavesLat.get(0) + " " + mSlavesLon.get(0));
            lp.leftMargin = canvasWidth / 2;
            lp.topMargin = canvasHeight / 2;

            Log.d("", "width: " + canvasWidth + " " + canvasWidth / 2);

            //add view to layout
            relativeLayout.addView(deviceTextview, lp);
        }

        else {
            // Get smallest and largest lats and longs to scale properly
            float smallestLon = Float.parseFloat(mSlavesLon.get(0));
            float largestLon = Float.parseFloat(mSlavesLon.get(0));
            float smallestLat = Float.parseFloat(mSlavesLat.get(0));
            float largestLat = Float.parseFloat(mSlavesLat.get(0));

            for (int i = 1; i < nSlaves; i++) {
                float lon = Float.parseFloat(mSlavesLon.get(i));
                float lat = Float.parseFloat(mSlavesLat.get(i));

                if (lon < smallestLon) {
                    smallestLon = lon;
                } else {
                    largestLon = lon;
                }

                if (lat < smallestLat) {
                    smallestLat = lat;
                } else {
                    largestLat = lat;
                }
            }

            float totalLonDiff = abs(largestLon - smallestLon);
            float totalLatDiff = abs(largestLat - smallestLat);

            Random r = new Random();
            for (int i = 0; i < nSlaves; i++) {
                TextView deviceTextview = new TextView(this);
                deviceTextview.setText(mNames.get(i) + " " + mSlavesLat.get(i) + " " + mSlavesLon.get(i));

                float lon = Float.parseFloat(mSlavesLon.get(i));
                float lat = Float.parseFloat(mSlavesLat.get(i));

                float lonDiff = abs(lon - smallestLon);
                float latDiff = abs(lat - smallestLat);

                double xScale = lonDiff / totalLonDiff;
                double yScale = latDiff / totalLatDiff;

                lp.leftMargin = 50 + (int) (xScale * (canvasWidth - 100));
                lp.topMargin = 50 + (int) (yScale * (canvasHeight - 100));

                //add view to layout
                relativeLayout.addView(deviceTextview, lp);
            }
        }
    }
}
