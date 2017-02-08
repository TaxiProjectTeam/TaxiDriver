package com.example.sveta.taxodriver.fragment;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sveta.taxodriver.R;
import com.example.sveta.taxodriver.adapter.OrderListAdapter;
import com.example.sveta.taxodriver.data.Order;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bohdan on 24.01.17.
 */

public class AllOrdersFragment extends Fragment implements ValueEventListener {

    private RecyclerView orderRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private OrderListAdapter listAdapter;
    private List<Order> currOrders;
    private FirebaseDatabase database;
    private DatabaseReference ref;
    ProgressDialog load;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_all_orders,container,false);


        load = ProgressDialog.show(getActivity(),getString(R.string.loading_text),getString(R.string.wait_loading_text));
        load.setCancelable(false);

        orderRecyclerView = (RecyclerView) rootView.findViewById(R.id.tab_all_orders_recycler_view);

        layoutManager = new LinearLayoutManager(getActivity());
        orderRecyclerView.setLayoutManager(layoutManager);

        database = FirebaseDatabase.getInstance();
        ref = database.getReference();

        ref.child("orders").addValueEventListener(this);

        currOrders = new ArrayList<Order>();
        listAdapter = new OrderListAdapter(getActivity(), currOrders);
        orderRecyclerView.setAdapter(listAdapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getActivity(),LinearLayoutManager.VERTICAL);
        orderRecyclerView.addItemDecoration(decoration);

        return rootView;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        currOrders.clear();
        for(DataSnapshot data : dataSnapshot.getChildren()){
            currOrders.add(data.getValue(Order.class));
        }
        listAdapter.notifyDataSetChanged();
        load.dismiss();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
