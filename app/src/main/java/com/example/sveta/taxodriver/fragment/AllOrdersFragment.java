package com.example.sveta.taxodriver.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sveta.taxodriver.R;

/**
 * Created by bohdan on 24.01.17.
 */

public class AllOrdersFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_all_orders,container,false);


    }
}
