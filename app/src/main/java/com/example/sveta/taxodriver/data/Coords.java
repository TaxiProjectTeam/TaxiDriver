package com.example.sveta.taxodriver.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by bohdan on 06.02.17.
 */

public class Coords implements Parcelable {

    public static final Parcelable.Creator<Coords> CREATOR = new Parcelable.Creator<Coords>() {
        @Override
        public Coords createFromParcel(Parcel source) {
            return new Coords(source);
        }

        @Override
        public Coords[] newArray(int size) {
            return new Coords[size];
        }
    };
    private double longitude;
    private double latitude;

    public Coords() {
    }

    public Coords(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    protected Coords(Parcel in) {
        this.longitude = in.readDouble();
        this.latitude = in.readDouble();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(this.longitude);
        dest.writeDouble(this.latitude);
    }
}
