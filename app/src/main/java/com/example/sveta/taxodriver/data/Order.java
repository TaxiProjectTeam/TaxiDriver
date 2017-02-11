package com.example.sveta.taxodriver.data;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sveta on 04.01.2017.
 */

public class Order {

    private Coords fromCoords;
    private Coords driverPos;
    private List<Coords> toCoords;
    private double price;
    private String driverId;
    private String status;
    private int time;

    public Order() {
    }

    public Order(Coords fromCoords, List<Coords> toCoords, Coords driverPos, double price, String driverId, int time, String status) {
        this.fromCoords = fromCoords;
        this.driverPos = driverPos;
        this.toCoords = toCoords;
        this.price = price;
        this.driverId = driverId;
        this.time = time;

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

    public List<Coords> getToCoords() {
        return toCoords;
    }

    public void setToCoords(ArrayList<Coords> toCoords) {
        this.toCoords = toCoords;
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

}
