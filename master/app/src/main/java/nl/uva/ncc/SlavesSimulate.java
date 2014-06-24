package nl.uva.ncc;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

import se.bitcraze.crazyfliecontrol.R;

public class SlavesSimulate extends Activity {

    ListView mListView;
    ArrayAdapter mAdapter;
    Arrow arrow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        arrow = new Arrow(this);
        setContentView(arrow);

        arrow.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrow.setSimulation(arrow.getDirection() + 10, arrow.getThrust() + 10);
            }
        });
    }
    
}
