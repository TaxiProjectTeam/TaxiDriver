package com.ck.taxoteam.taxodriver.fragment;


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

import com.ck.taxoteam.taxodriver.R;
import com.ck.taxoteam.taxodriver.activity.MainActivity;
import com.ck.taxoteam.taxodriver.activity.OrderDetailsActivity;
import com.ck.taxoteam.taxodriver.adapter.OrderListAdapter;
import com.ck.taxoteam.taxodriver.data.Order;

import java.util.ArrayList;
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

        //Recycler
        orderRecyclerView = (RecyclerView) rootView.findViewById(R.id.tab_my_orders_recycler_view);
        layoutManager = new LinearLayoutManager(getActivity());
        orderRecyclerView.setLayoutManager(layoutManager);

        //Item decoration
        DividerItemDecoration decoration = new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL);
        orderRecyclerView.addItemDecoration(decoration);

        //Register listener
        ((MainActivity) getActivity()).registerOnDataReadyListener(this);

        return rootView;
    }

    @Override
    public void onResume() {
        //Get orders list
        currOrders = new ArrayList<Order>();
        currOrders.addAll(((MainActivity) getActivity()).getMyOrders());

        //Orders adapter
        listAdapter = new OrderListAdapter(getActivity(), currOrders, this);
        orderRecyclerView.setAdapter(listAdapter);

        super.onResume();
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
            currOrders.clear();
            currOrders.addAll(data);
            listAdapter.notifyDataSetChanged();

        }
    }
}
