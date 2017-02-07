package com.example.sveta.taxodriver.data;

/**
 * Created by bohdan on 06.02.17.
 */

public class Coords {

    private double longitude;
    private double latitude;

    public Coords() {
    }

    public Coords(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
