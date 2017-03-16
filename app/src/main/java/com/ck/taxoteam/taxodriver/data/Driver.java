package com.ck.taxoteam.taxodriver.data;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by bohdan on 29.01.17.
 */
@IgnoreExtraProperties
public class Driver {

    private String name;
    private String phoneNumber;
    private String carModel;
    private String carNumber;

    public Driver(){}

    public Driver(String name, String phoneNumber, String carModel, String carNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.carModel = carModel;
        this.carNumber = carNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }
}
