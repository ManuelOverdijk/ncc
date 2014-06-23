package nl.uva.ncc;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.Window;

import java.util.ArrayList;

import se.bitcraze.crazyfliecontrol.R;
import android.content.Intent;

public class SlavesActivity extends Activity {

    ListView mListView;
    ArrayAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slaves);

        ArrayList<Slave> items = new ArrayList<Slave>();
        items.add(new Slave());
        items.add(new Slave());

        mAdapter = new SlaveAdapter(this, R.layout.view_slave_item, items);
        mListView = (ListView)findViewById(R.id.listView);
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new ListView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(parent.getContext(), SlavesSimulate.class);
                startActivity(intent);
            }
        });
    }


    
}
