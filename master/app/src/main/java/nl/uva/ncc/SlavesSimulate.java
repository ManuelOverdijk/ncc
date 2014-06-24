package nl.uva.ncc;

/**
 * Created by koen on 24-6-14.
 */
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
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

    private void visualize_devices() {
        Intent intent = getIntent();
        // get the names and coordinates of connected devices
        ArrayList<String> mNames = intent.getStringArrayListExtra("slavesnames");
        ArrayList<String> mSlavesLat = intent.getStringArrayListExtra("slaveslat");
        ArrayList<String> mSlavesLon = intent.getStringArrayListExtra("slaveslon");

        // add new textview with info to layout for every device
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relative_layout);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        Random r = new Random();
        for(int i = 0; i < mNames.size(); i++) {
            TextView deviceTextview = new TextView(this);
            deviceTextview.setText(mNames.get(i) + " " + mSlavesLat.get(i) + " " + mSlavesLon.get(i));

            //TODO.. set margin relative to coordinates
            lp.leftMargin = 50 + r.nextInt(500);
            lp.topMargin = 50 + r.nextInt(500);

            //add view to layout
            relativeLayout.addView(deviceTextview, lp);
        }
    }

}
