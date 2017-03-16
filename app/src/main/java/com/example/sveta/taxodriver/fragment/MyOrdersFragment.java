package com.example.sveta.taxodriver.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sveta.taxodriver.R;
import com.example.sveta.taxodriver.activity.MainActivity;
import com.example.sveta.taxodriver.activity.OrderDetailsActivity;
import com.example.sveta.taxodriver.adapter.OrderListAdapter;
import com.example.sveta.taxodriver.data.Order;

import java.util.List;

/**
 * Created by bohdan on 24.01.17.
 */

public class MyOrdersFragment extends Fragment implements OrderListAdapter.ItemClickListener, MainActivity.OnDataReadyListener {
    public static final boolean ORDER_TYPE_MY = false;
    private RecyclerView orderRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private OrderListAdapter listAdapter;
    private List<Order> currOrders;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_my_orders, container, false);

        //Get orders list
        currOrders = ((MainActivity) getActivity()).getMyOrders();

        //Recycler
        orderRecyclerView = (RecyclerView) rootView.findViewById(R.id.tab_my_orders_recycler_view);
        layoutManager = new LinearLayoutManager(getActivity());
        orderRecyclerView.setLayoutManager(layoutManager);

        //Orders list
        listAdapter = new OrderListAdapter(getActivity(), currOrders, this);
        orderRecyclerView.setAdapter(listAdapter);

        //Item decoration
        DividerItemDecoration decoration = new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL);
        orderRecyclerView.addItemDecoration(decoration);

        //Register listener
        ((MainActivity) getActivity()).registerOnDataReadyListener(this);

        return rootView;
    }

    @Override
    public void onItemClick(Order order, int position) {
        Intent intent = new Intent(getActivity(), OrderDetailsActivity.class);
        intent.putExtra("order", order);
        startActivity(intent);
    }

    @Override
    public void onDataReady(List<Order> data, boolean orderType) {
        if (orderType == ORDER_TYPE_MY) {
            currOrders = data;
            listAdapter = new OrderListAdapter(getActivity(), currOrders, this);
            orderRecyclerView.setAdapter(listAdapter);
        }
    }
}
