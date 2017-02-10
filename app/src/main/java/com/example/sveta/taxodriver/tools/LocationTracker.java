package com.example.sveta.taxodriver.tools;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.example.sveta.taxodriver.data.Coords;

/**
 * Created by bogdan on 10.02.17.
 */

public class LocationTracker implements LocationListener {
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 60000;
    private LocationManager locationManager;
    private Context context;
    private Location location;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;

    public LocationTracker(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public Coords getCurrentLocation() {


        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
            }

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }
        }
        if (location != null) {
            return new Coords(location.getLongitude(), location.getLatitude());
        }

        //Center of Cherkasy (default coords)
        return new Coords(32.059769, 49.444431);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
