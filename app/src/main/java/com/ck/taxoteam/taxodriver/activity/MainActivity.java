package com.ck.taxoteam.taxodriver.activity;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.ck.taxoteam.taxodriver.R;
import com.ck.taxoteam.taxodriver.data.CurrentDriver;
import com.ck.taxoteam.taxodriver.data.Driver;
import com.ck.taxoteam.taxodriver.data.Order;
import com.ck.taxoteam.taxodriver.fragment.AboutProgramFragment;
import com.ck.taxoteam.taxodriver.fragment.OrdersListFragment;
import com.ck.taxoteam.taxodriver.fragment.UserInfoFragment;
import com.ck.taxoteam.taxodriver.tools.LocationConverter;
import com.ck.taxoteam.taxodriver.tools.PermitionsHelper;
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

    GoogleApiClient client;
    private Toolbar toolbar;
    private List<Order> freeOrders;
    private List<Order> myOrders;
    private FirebaseUser user;
    private DatabaseReference ref;
    private FirebaseDatabase database;
    private Location currLocation;
    private List<OnDataReadyListener> onDataReadyListeners;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onDataReadyListeners = new ArrayList<OnDataReadyListener>();

        //Get info from firebase about current driver
        getCurrentUserInformation();

        //Restore arrays
        if(savedInstanceState != null){
            freeOrders = savedInstanceState.getParcelableArrayList("allArray");
            myOrders = savedInstanceState.getParcelableArrayList("myArray");
        }
        else{
            freeOrders = new ArrayList<Order>();
            myOrders = new ArrayList<Order>();
            getSupportFragmentManager().beginTransaction().add(R.id.main_fragments_container, new OrdersListFragment()).commit();
        }


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Navigation Drawer
        attachNavigationDrawer();

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

        //Toolbox progress bar
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //database listener
        ref.child("orders").addValueEventListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        client.connect();
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
        ProcessingDataTask task = new ProcessingDataTask(this);
        //Processing data async
        task.execute(dataSnapshot);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

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
        if (PermitionsHelper.checkLocationPermitions(this)) {
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
    protected void onDestroy() {

        ref.child("orders").removeEventListener(this);
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("allArray", (ArrayList<? extends Parcelable>) freeOrders);
        outState.putParcelableArrayList("myArray", (ArrayList<? extends Parcelable>) myOrders);
        super.onSaveInstanceState(outState);
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

    private class ProcessingDataTask extends AsyncTask<DataSnapshot,Void,Void> {
        private Context context;
        private List<Order> tempFreeOrders;
        private List<Order> tempMyOrders;

        public ProcessingDataTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(DataSnapshot... params) {

            tempFreeOrders = new ArrayList<Order>();
            tempMyOrders = new ArrayList<Order>();
            for (DataSnapshot data : params[0].getChildren()) {
                Order order = data.getValue(Order.class);
                order.setId(data.getKey());
                order.setFromAddress(LocationConverter.getCompleteAddressString(context,
                        order.getFromCoords().getLatitude(),
                        order.getFromCoords().getLongitude()));
                List<String> toAddress = new ArrayList<String>();
                for (int i = 0; i < order.getToCoords().size(); i++) {
                    toAddress.add(LocationConverter.getCompleteAddressString(context,
                            order.getToCoords().get(i).getLatitude(),
                            order.getToCoords().get(i).getLongitude()));
                }
                order.setToAdress(toAddress);
                if (order.getStatus().equals("free")) {
                    tempFreeOrders.add(order);
                } else if (order.getDriverId().equals(user.getUid())) {
                    tempMyOrders.add(order);
                }
            }
            //Sort (minimal distance)
            sortOrders(tempFreeOrders);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //Send messages to fragments
            for (OnDataReadyListener listener : onDataReadyListeners) {
                freeOrders = tempFreeOrders;
                myOrders = tempMyOrders;
                listener.onDataReady(freeOrders, ORDER_TYPE_FREE);
                listener.onDataReady(myOrders, ORDER_TYPE_MY);
            }

            progressBar.setVisibility(View.GONE);
        }
    }

}

