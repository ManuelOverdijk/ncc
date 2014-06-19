package com.example.projectdroneserver.app;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.LinkedList;

public class GPSLocationProvider {
    static final int NUM_GPS_MEASUREMENTS = 20;
    static final int MIN_ACCURACY = 15;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private LinkedList<Location> locations;


    public GPSLocationProvider(Context context) {
        locations = new LinkedList<Location>();

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            // Called when a new location is found by the network location provider.
            public void onLocationChanged(Location location) {
                // Reset measurements when device moves
                if (location.getSpeed() > 0) {
                    resetMeasurements();
                    return;
                }

                // Discard location if not accurate enough
                if (location.getAccuracy() <= MIN_ACCURACY) {
                    locations.addLast(location);
                }

                // Keep list of locations up to date by removing older measurements
                if (locations.size() == NUM_GPS_MEASUREMENTS + 1) {
                    locations.removeFirst();
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };
    }

    Location getLocation() {
        if (locations.isEmpty()) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                return location;
            } else {
                location = new Location("myProvider");
                location.setLatitude(Double.NaN);
                location.setLongitude(Double.NaN);
            }
        }

        double avgLat = 0;
        double avgLong = 0;
        float totalWeight = 0;
        float weight = 0;

        // Calculate weighted average of locations
        for (Location location : locations) {
            weight = MIN_ACCURACY - location.getAccuracy();
            avgLat += location.getLatitude() * weight;
            avgLong += location.getLongitude() * weight;

            totalWeight += weight;
        }

        avgLat /= totalWeight;
        avgLong /= totalWeight;

        // Return location
        Location location = new Location("myProvider");
        location.setLatitude(avgLat);
        location.setLongitude(avgLong);

        return location;
    }

    void startMeasurements() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    void stopMeasurements() {
        locationManager.removeUpdates(locationListener);
    }

    void resetMeasurements() {
        locations.clear();
    }

}
