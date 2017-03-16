package com.ck.taxoteam.taxodriver.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.ck.taxoteam.taxodriver.R;
import com.ck.taxoteam.taxodriver.fragment.AllOrdersFragment;
import com.ck.taxoteam.taxodriver.fragment.MyOrdersFragment;

/**
 * Created by bohdan on 24.01.17.
 */

public class TabPagerAdapter extends FragmentStatePagerAdapter {

    int tabCount;
    Context context;

    public TabPagerAdapter(Context context, FragmentManager fm, int tabCount) {
        super(fm);
        this.tabCount = tabCount;
        this.context = context;
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
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getResources().getString(R.string.all_orders_text);
            case 1:
                return context.getResources().getString(R.string.my_orders_text);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }
}
