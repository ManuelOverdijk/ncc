package com.example.mymodule.app2;

import java.io.Serializable;

/**
 * Created by datwelk on 19/06/14.
 */
public class Slave implements Serializable {
    private String mIdentifier;
    private double mLatitude;
    private double mLongitude;

    public Slave() {
        this.mIdentifier = "Device ID";
    }

    public void setIdentifier(String identifier) {
        this.mIdentifier = identifier;
    }

    public String getIdentifier() {
        return this.mIdentifier;
    }

    public double getLatitude() {
        return this.mLatitude;
    }

    public void setLatitude(double latitude) {
        this.mLatitude = latitude;
    }

    public double getLongitude() {
        return this.mLongitude;
    }

    public void setLongitude(double longitude) {
        this.mLongitude = longitude;
    }
}