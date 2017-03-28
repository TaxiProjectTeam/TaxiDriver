package com.ck.taxoteam.taxodriver.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
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

public class AllOrdersFragment extends Fragment implements OrderListAdapter.ItemClickListener, MainActivity.OnDataReadyListener {


    public static final boolean ORDER_TYPE_FREE = true;
    private RecyclerView orderRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private OrderListAdapter listAdapter;
    private List<Order> currOrders;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_all_orders,container,false);

        //Recycler view init
        orderRecyclerView = (RecyclerView) rootView.findViewById(R.id.tab_all_orders_recycler_view);
        layoutManager = new LinearLayoutManager(getActivity());
        orderRecyclerView.setLayoutManager(layoutManager);

        //Item decoration
        DividerItemDecoration decoration = new DividerItemDecoration(getActivity(),LinearLayoutManager.VERTICAL);
        orderRecyclerView.addItemDecoration(decoration);

        //Register on data ready listener
        ((MainActivity) getActivity()).registerOnDataReadyListener(this);

        //Get order list
        currOrders = new ArrayList<Order>();
        currOrders.addAll(((MainActivity) getActivity()).getFreeOrders());

        //Adapter
        listAdapter = new OrderListAdapter(getActivity(), currOrders, this);
        listAdapter.setHasStableIds(true);
        orderRecyclerView.setAdapter(listAdapter);

        //Restore layout manager (for save scroll position)
        if(null != savedInstanceState){
            Parcelable state = savedInstanceState.getParcelable("layoutManager");
            layoutManager.onRestoreInstanceState(state);
        }


        return rootView;
    }

    //On item click listener
    @Override
    public void onItemClick(Order order, int position) {
        Intent detailsScreenIntent = new Intent(getActivity(), OrderDetailsActivity.class);
        detailsScreenIntent.putExtra("order", order);
        startActivity(detailsScreenIntent);
    }

    @Override
    public void onDataReady(List<Order> data, boolean orderType) {
        if (orderType == ORDER_TYPE_FREE) {
            currOrders.clear();
            currOrders.addAll(data);
            listAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("layoutManager", layoutManager.onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }
}
