package nl.uva.ncc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import se.bitcraze.crazyfliecontrol.R;
import com.example.mymodule.app2.DevicePacket;

/**
 * Created by datwelk on 19/06/14.
 */
public class SlaveAdapter extends ArrayAdapter {
    private ArrayList<DevicePacket> mItems;

    public SlaveAdapter(Context context, int textViewResourceId, ArrayList<DevicePacket> items) {
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

        DevicePacket devicePacket = this.mItems.get(position);
        String latitude = String.valueOf(devicePacket.getLatitude());
        String longitude = String.valueOf(devicePacket.getLongitude());
        String locationString = "Lat: " + latitude + ", Lon: " + longitude;

        TextView textViewCoordinates = (TextView)view.findViewById(R.id.textViewCoordinates);
        textViewCoordinates.setText(locationString);

        TextView textViewDeviceId = (TextView)view.findViewById(R.id.textViewDeviceID);
        if (devicePacket.getName() == null || devicePacket.getName().length() == 0)
            textViewDeviceId.setText(devicePacket.getIdentifier());
        else
            textViewDeviceId.setText(devicePacket.getName());

        return view;
    }
}
