package com.example.sveta.taxodriver.fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by bohdan on 24.01.17.
 */

public class AllOrdersFragment extends Fragment implements ValueEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 13;
    ProgressDialog load;
    Location currLocation;
    GoogleApiClient client;
    private RecyclerView orderRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private OrderListAdapter listAdapter;
    private List<Order> currOrders;
    private FirebaseDatabase database;
    private DatabaseReference ref;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_all_orders,container,false);

        //Show progress dialog
        load = ProgressDialog.show(getActivity(),getString(R.string.loading_text),getString(R.string.wait_loading_text));
        load.setCancelable(false);

        orderRecyclerView = (RecyclerView) rootView.findViewById(R.id.tab_all_orders_recycler_view);

        layoutManager = new LinearLayoutManager(getActivity());
        orderRecyclerView.setLayoutManager(layoutManager);

        database = FirebaseDatabase.getInstance();
        ref = database.getReference();

        ref.child("orders").addValueEventListener(this);

        currOrders = new ArrayList<Order>();

        // On item click
        listAdapter = new OrderListAdapter(getActivity(), currOrders, new OrderListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(Order order, int position) {
                Intent detailsScreenIntent = new Intent(getActivity(), OrderDetailsActivity.class);
                detailsScreenIntent.putExtra("order", order);
                startActivity(detailsScreenIntent);
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

        DividerItemDecoration decoration = new DividerItemDecoration(getActivity(),LinearLayoutManager.VERTICAL);
        orderRecyclerView.addItemDecoration(decoration);

        currLocation = new Location("current location");
        currLocation.setLatitude(49.444431);
        currLocation.setLongitude(32.059769);

        if (client == null) {
            client = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        return rootView;
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {


        currOrders.clear();
        for(DataSnapshot data : dataSnapshot.getChildren()){
            Order order = data.getValue(Order.class);
            if (order.getStatus().equals("free")) {
                order.setId(data.getKey());
                currOrders.add(order);
            }
        }


        //Sort (minimal distance)
        Collections.sort(currOrders, new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                Location locationA = new Location("A");
                locationA.setLatitude(o1.getFromCoords().getLatitude());
                locationA.setLongitude(o1.getFromCoords().getLongitude());
                Location locationB = new Location("B");
                locationB.setLatitude(o2.getFromCoords().getLatitude());
                locationB.setLongitude(o2.getFromCoords().getLongitude());

                try {
                    double difference = locationA.distanceTo(currLocation) - locationB.distanceTo(currLocation);

                    if (difference > 0) {
                        return 1;
                    }
                    return -1;
                } catch (NullPointerException e) {
                    return -1;
                }
            }
        });
        listAdapter.notifyDataSetChanged();
        load.dismiss();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
            currLocation = LocationServices.FusedLocationApi.getLastLocation(client);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onStart() {
        super.onStart();
        client.connect();
    }
}
