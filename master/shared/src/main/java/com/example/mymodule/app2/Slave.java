package com.example.mymodule.app2;

import java.io.Serializable;

/**
 * Created by datwelk on 19/06/14.
 */
public class Slave implements Serializable {
    static final long serialVersionUID = 1337;
    private String mIdentifier;
    private double mLatitude;
    private double mLongitude;

    public Slave() {
        this.mIdentifier = "Device ID";
        mLatitude = Double.NaN;
        mLongitude = Double.NaN;
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

    @Override
    public boolean equals(Object o) {
        return o instanceof Slave &&
                ((Slave) o).getIdentifier().equals(this.getIdentifier());
    }

    @Override
    public String toString() {
        String returnString = "Device Identifier: " + this.mIdentifier;

        return returnString;
    }
}