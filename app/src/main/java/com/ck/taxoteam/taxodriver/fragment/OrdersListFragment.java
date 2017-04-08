package com.ck.taxoteam.taxodriver.fragment;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ck.taxoteam.taxodriver.R;
import com.ck.taxoteam.taxodriver.adapter.TabPagerAdapter;


public class OrdersListFragment extends Fragment implements TabLayout.OnTabSelectedListener {


    private TabLayout tabLayout;
    private ViewPager viewPager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_orders_list, container, false);

        //Initialize
        tabLayout = (TabLayout) rootView.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.all_orders_text));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.my_orders_text));

        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getActivity(), getActivity().getSupportFragmentManager(), tabLayout.getTabCount());

        //View pager
        viewPager = (ViewPager) rootView.findViewById(R.id.tab_pager);
        viewPager.setAdapter(pagerAdapter);

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(this);
        return rootView;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}
