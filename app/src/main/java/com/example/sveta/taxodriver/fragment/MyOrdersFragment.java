package com.example.sveta.taxodriver.fragment;


import android.app.ProgressDialog;
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
import com.example.sveta.taxodriver.activity.OrderDetailsActivity;
import com.example.sveta.taxodriver.adapter.OrderListAdapter;
import com.example.sveta.taxodriver.data.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class MyOrdersFragment extends Fragment implements ValueEventListener {
    private RecyclerView orderRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private OrderListAdapter listAdapter;
    private List<Order> currOrders;
    private ProgressDialog load;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference ref;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_my_orders, container, false);
        //Show progress dialog
        load = ProgressDialog.show(getActivity(), getString(R.string.loading_text), getString(R.string.wait_loading_text));
        load.setCancelable(false);

        orderRecyclerView = (RecyclerView) rootView.findViewById(R.id.tab_my_orders_recycler_view);

        layoutManager = new LinearLayoutManager(getActivity());
        orderRecyclerView.setLayoutManager(layoutManager);

        database = FirebaseDatabase.getInstance();
        ref = database.getReference();

        ref.child("orders").addValueEventListener(this);

        currOrders = new ArrayList<Order>();
        listAdapter = new OrderListAdapter(getActivity(), currOrders, new OrderListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(Order order, int position) {
                Intent intent = new Intent(getActivity(), OrderDetailsActivity.class);
                startActivity(intent);
            }
        });
        listAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                load.dismiss();
            }
        });
        orderRecyclerView.setAdapter(listAdapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL);
        orderRecyclerView.addItemDecoration(decoration);

        user = FirebaseAuth.getInstance().getCurrentUser();

        return rootView;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        currOrders.clear();
        for (DataSnapshot data : dataSnapshot.getChildren()) {
            Order order = data.getValue(Order.class);
            if (order.getStatus().equals("accepted") && order.getDriverId().equals(user.getUid())) {
                currOrders.add(data.getValue(Order.class));
            }
        }
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
