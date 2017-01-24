package com.example.sveta.taxodriver.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.sveta.taxodriver.fragment.AllOrdersFragment;
import com.example.sveta.taxodriver.fragment.MyOrdersFragment;

/**
 * Created by bohdan on 24.01.17.
 */

public class TabPagerAdapter extends FragmentStatePagerAdapter {

    int tabCount;

    public TabPagerAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                AllOrdersFragment fragment = new AllOrdersFragment();
                return fragment;
            case 1:
                MyOrdersFragment fragment1 = new MyOrdersFragment();
                return fragment1;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
