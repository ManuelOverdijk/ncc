package nl.uva.ncc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import se.bitcraze.crazyfliecontrol.R;

/**
 * Created by datwelk on 19/06/14.
 */
public class SlaveAdapter extends ArrayAdapter {
    private ArrayList<Slave> mItems;

    public SlaveAdapter(Context context, int textViewResourceId, ArrayList<Slave> items) {
        super(context, textViewResourceId, items);
        this.mItems = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.view_slave_item, null);
        }

        Slave slave = this.mItems.get(position);
        String latitude = String.valueOf(slave.getLocation().getLatitude());
        String longitude = String.valueOf(slave.getLocation().getLongitude());
        String locationString = "Lat: " + latitude + ", Lon: " + longitude;

        TextView textViewCoordinates = (TextView)view.findViewById(R.id.textViewCoordinates);
        textViewCoordinates.setText(locationString);

        TextView textViewDeviceId = (TextView)view.findViewById(R.id.textViewDeviceID);
        textViewDeviceId.setText(slave.getIdentifier());

        return view;
    }
}