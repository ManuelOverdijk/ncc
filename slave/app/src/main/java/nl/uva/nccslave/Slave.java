package nl.uva.nccslave;

import android.location.Location;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by datwelk on 19/06/14.
 */
public class Slave implements Serializable {
    private String mIdentifier;
    private Location mLocation;

    public Slave() {
        this.mIdentifier = "Device ID";

        Location location = new Location("");
        location.setLatitude(1.0);
        location.setLongitude(50.0);
        this.mLocation = location;
    }

    public Slave(String identifier, Location location) {
        this.mIdentifier = identifier;
        this.mLocation = location;
    }

    public void setIdentifier(String identifier) {
        this.mIdentifier = identifier;
    }

    public String getIdentifier() {
        return this.mIdentifier;
    }

    public Location getLocation() {
        return this.mLocation;
    }

    public void setLocation(Location location) {
        this.mLocation = location;
    }
}
