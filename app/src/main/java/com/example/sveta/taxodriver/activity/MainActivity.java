package com.example.sveta.taxodriver.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.sveta.taxodriver.R;
import com.example.sveta.taxodriver.data.CurrentDriver;
import com.example.sveta.taxodriver.data.Driver;
import com.example.sveta.taxodriver.data.Order;
import com.example.sveta.taxodriver.fragment.AboutProgramFragment;
import com.example.sveta.taxodriver.fragment.OrdersListFragment;
import com.example.sveta.taxodriver.fragment.ProgressFragment;
import com.example.sveta.taxodriver.fragment.UserInfoFragment;
import com.example.sveta.taxodriver.tools.LocationConverter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends AppCompatActivity implements ValueEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final boolean ORDER_TYPE_FREE = true;
    public static final boolean ORDER_TYPE_MY = false;
    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 13;
    GoogleApiClient client;
    private Toolbar toolbar;
    private List<Order> freeOrders;
    private List<Order> myOrders;
    private FirebaseUser user;
    private DatabaseReference ref;
    private FirebaseDatabase database;
    private Location currLocation;
    private List<OnDataReadyListener> onDataReadyListeners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onDataReadyListeners = new ArrayList<OnDataReadyListener>();

        //Get info from firebase about current driver
        getCurrentUserInformation();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.main_fragments_container, new ProgressFragment()).commit();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Navigation Drawer
        attachNavigationDrawer();

        //Data lists
        freeOrders = new ArrayList<Order>();
        myOrders = new ArrayList<Order>();

        user = FirebaseAuth.getInstance().getCurrentUser();

        //Firebase database init
        database = FirebaseDatabase.getInstance();
        ref = database.getReference();

        //default - center of cherkasy
        currLocation = new Location("current location");
        currLocation.setLatitude(49.444431);
        currLocation.setLongitude(32.059769);

        //init google api client
        if (client == null) {
            client = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_menu_item:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
        }
        return false;
    }

    private void getCurrentUserInformation() {
        final Driver[] currDriver = {new Driver()};
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            ref.child("drivers").child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //get Data about user from server
                    currDriver[0] = dataSnapshot.getValue(Driver.class);
                    CurrentDriver.setInstance(currDriver[0]);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void attachNavigationDrawer() {
        final Drawer drawer = new DrawerBuilder()
                .withActivity(this)
                .withDrawerWidthDp(320)
                .withToolbar(toolbar)
                .withHeader(R.layout.drawer_header)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.text_orders_list_drawer),
                        new PrimaryDrawerItem().withName(R.string.text_driver_info),
                        new PrimaryDrawerItem().withName(R.string.text_about_program)
                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        //Replace fragments
                        switch (position) {
                            case 1:
                                getSupportFragmentManager().beginTransaction().
                                        replace(R.id.main_fragments_container, new OrdersListFragment()).
                                        commit();
                                return false;
                            case 2:
                                getSupportFragmentManager().beginTransaction().
                                        replace(R.id.main_fragments_container, new UserInfoFragment()).
                                        commit();
                                return false;
                            case 3:
                                getSupportFragmentManager().beginTransaction().
                                        replace(R.id.main_fragments_container, new AboutProgramFragment()).
                                        commit();
                                return false;
                            default:
                                return true;
                        }
                    }
                }).build();

    }

    //get data from firebase
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        freeOrders.clear();
        myOrders.clear();
        for (DataSnapshot data : dataSnapshot.getChildren()) {
            Order order = data.getValue(Order.class);
            order.setId(data.getKey());
            order.setFromAddress(LocationConverter.getCompleteAddressString(this,
                    order.getFromCoords().getLatitude(),
                    order.getFromCoords().getLongitude()));
            List<String> toAddress = new ArrayList<String>();
            for (int i = 0; i < order.getToCoords().size(); i++) {
                toAddress.add(LocationConverter.getCompleteAddressString(this,
                        order.getToCoords().get(i).getLatitude(),
                        order.getToCoords().get(i).getLongitude()));
            }
            order.setToAdress(toAddress);
            if (order.getStatus().equals("free")) {
                freeOrders.add(order);
            } else if (order.getStatus().equals("accepted") && order.getDriverId().equals(user.getUid())) {
                myOrders.add(order);
            }
        }

        //Sort (minimal distance)
        sortOrders(freeOrders);
        sortOrders(myOrders);

        //Send messages to fragments
        for (OnDataReadyListener listener : onDataReadyListeners) {
            listener.onDataReady(freeOrders, ORDER_TYPE_FREE);
            listener.onDataReady(myOrders, ORDER_TYPE_MY);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragments_container, new OrdersListFragment()).commit();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        ref.child("orders").addValueEventListener(this);
        client.connect();
    }

    private void sortOrders(List<Order> currOrders) {
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
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
        }
        currLocation = LocationServices.FusedLocationApi.getLastLocation(client);
        sortOrders(freeOrders);
        sortOrders(myOrders);
        for (OnDataReadyListener listener : onDataReadyListeners) {
            listener.onDataReady(freeOrders, ORDER_TYPE_FREE);
            listener.onDataReady(myOrders, ORDER_TYPE_MY);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void registerOnDataReadyListener(OnDataReadyListener listener) {
        onDataReadyListeners.add(listener);
    }

    public List<Order> getFreeOrders() {
        return freeOrders;
    }

    public List<Order> getMyOrders() {
        return myOrders;
    }

    public interface OnDataReadyListener {
        void onDataReady(List<Order> data, boolean orderType);
    }
}

