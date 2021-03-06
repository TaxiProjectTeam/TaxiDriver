package com.ck.taxoteam.taxodriver.data;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;



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
    private String price;
    private String driverId;
    private String status;
    private boolean meAccept = false;
    private String fromAddress = "";
    private List<String> toAdress = new ArrayList<String>();

    private int time;

    public Order() {
    }

    public Order(String id, String additionalComment, Coords fromCoords, Coords driverPos, ArrayList<Coords> toCoords, String price, String driverId, String status, int time) {
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
        this.price = in.readString();
        this.driverId = in.readString();
        this.status = in.readString();
        this.time = in.readInt();
    }
    @Exclude
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

    @Exclude
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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
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

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public List<String> getToAdress() {
        return toAdress;
    }

    public void setToAdress(List<String> toAdress) {
        this.toAdress = toAdress;
    }

    @Exclude
    public boolean isMeAccept() {
        return meAccept;
    }

    public void setMeAccept(boolean meAccept) {
        this.meAccept = meAccept;
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
        dest.writeString(this.price);
        dest.writeString(this.driverId);
        dest.writeString(this.status);
        dest.writeInt(this.time);
    }
}
