package com.example.sveta.taxodriver.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sveta.taxodriver.R;
import com.example.sveta.taxodriver.data.CurrentDriver;
import com.example.sveta.taxodriver.data.Driver;

/**
 * Created by bohdan on 24.01.17.
 */

public class UserInfoFragment extends Fragment {

    TextView nameTextView;
    TextView phoneTextView;
    TextView carModelTextView;
    TextView carNumberTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootVeiw = inflater.inflate(R.layout.fragment_user_info,container,false);
        nameTextView = (TextView) rootVeiw.findViewById(R.id.text_driver_info_name);
        phoneTextView = (TextView) rootVeiw.findViewById(R.id.text_driver_info_phone);
        carModelTextView = (TextView) rootVeiw.findViewById(R.id.text_driver_car_model);
        carNumberTextView = (TextView) rootVeiw.findViewById(R.id.text_driver_car_number);

        return rootVeiw;

    }

    @Override
    public void onStart() {
        super.onStart();
        Driver driver = CurrentDriver.getInstance();
        nameTextView.setText(driver.getName());
        phoneTextView.setText(driver.getPhoneNumber());
        carModelTextView.setText(driver.getCarModel());
        carNumberTextView.setText(driver.getCarNumber());
    }
}
