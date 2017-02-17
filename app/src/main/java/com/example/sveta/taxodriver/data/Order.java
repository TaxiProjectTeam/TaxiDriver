package com.example.sveta.taxodriver.data;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Sveta on 04.01.2017.
 */

public class Order implements Parcelable {
    public static final Parcelable.Creator<Order> CREATOR = new Parcelable.Creator<Order>() {
        @Override
        public Order createFromParcel(Parcel source) {
            return new Order(source);
        }

        @Override
        public Order[] newArray(int size) {
            return new Order[size];
        }
    };
    private String id;
    private String additionalComment;
    private Coords fromCoords;
    private Coords driverPos;
    private ArrayList<Coords> toCoords;
    private double price;
    private String driverId;
    private String status;
    private int time;

    public Order() {
    }

    public Order(String id, String additionalComment, Coords fromCoords, Coords driverPos, ArrayList<Coords> toCoords, double price, String driverId, String status, int time) {
        this.id = id;
        this.additionalComment = additionalComment;
        this.fromCoords = fromCoords;
        this.driverPos = driverPos;
        this.toCoords = toCoords;
        this.price = price;
        this.driverId = driverId;
        this.status = status;
        this.time = time;
    }

    protected Order(Parcel in) {
        this.id = in.readString();
        this.additionalComment = in.readString();
        this.fromCoords = in.readParcelable(Coords.class.getClassLoader());
        this.driverPos = in.readParcelable(Coords.class.getClassLoader());
        this.toCoords = new ArrayList<Coords>();
        in.readList(this.toCoords, Coords.class.getClassLoader());
        this.price = in.readDouble();
        this.driverId = in.readString();
        this.status = in.readString();
        this.time = in.readInt();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Coords getDriverPos() {
        return driverPos;
    }

    public void setDriverPos(Coords driverPos) {
        this.driverPos = driverPos;
    }

    public Coords getFromCoords() {
        return fromCoords;
    }

    public void setFromCoords(Coords fromCoords) {
        this.fromCoords = fromCoords;
    }

    public ArrayList<Coords> getToCoords() {
        return toCoords;
    }

    public void setToCoords(ArrayList<Coords> toCoords) {
        this.toCoords = toCoords;
    }

    public String getAdditionalComment() {
        return additionalComment;
    }

    public void setAdditionalComment(String additionalComment) {
        this.additionalComment = additionalComment;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.additionalComment);
        dest.writeParcelable(this.fromCoords, flags);
        dest.writeParcelable(this.driverPos, flags);
        dest.writeList(this.toCoords);
        dest.writeDouble(this.price);
        dest.writeString(this.driverId);
        dest.writeString(this.status);
        dest.writeInt(this.time);
    }
}
