package com.example.mymodule.app2;

import java.io.Serializable;

enum DeviceType {
    SLAVE, DRONE, UNKNOWN
}

/**
 * Created by datwelk on 19/06/14.
 */
public class DevicePacket implements Serializable {
    static final long serialVersionUID = 1337;
    private String mIdentifier;
    private String mName;
    private DeviceType mDeviceType;
    private double mLatitude;
    private double mLongitude;
    private double bearing;

    public DevicePacket() {
        this.mIdentifier = "Device ID";
        this.mName = null;
        this.mDeviceType = DeviceType.UNKNOWN;
        this.mLatitude = Double.NaN;
        this.mLongitude = Double.NaN;
        this.bearing = Double.NaN;
    }

    public void setIdentifier(String identifier) {
        this.mIdentifier = getTrimmedMAC(identifier);
    }

    public String getIdentifier() {
        return this.mIdentifier;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getName() {
        return this.mName;
    }

    public void setDeviceType(DeviceType mDeviceType) { this.mDeviceType = mDeviceType; }

    public DeviceType getDeviceType() { return this.mDeviceType; }

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

    public static String getTrimmedMAC(String mac) {
        return mac.substring(3);
    }

    public double getBearing() {
        return this.bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof DevicePacket &&
                ((DevicePacket) o).getIdentifier().equals(this.getIdentifier()));
    }

    @Override
    public String toString() {
        String returnString = "Device Identifier: " + this.mIdentifier +
                ", latitude: " + mLatitude +
                ", longitude: " + mLongitude;

        return returnString;
    }
}