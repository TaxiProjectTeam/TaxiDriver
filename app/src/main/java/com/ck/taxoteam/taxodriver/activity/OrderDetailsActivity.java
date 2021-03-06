package com.ck.taxoteam.taxodriver.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ck.taxoteam.taxodriver.R;
import com.ck.taxoteam.taxodriver.adapter.AddressListAdapter;
import com.ck.taxoteam.taxodriver.data.Coords;
import com.ck.taxoteam.taxodriver.data.Order;
import com.ck.taxoteam.taxodriver.receiver.NetworkStateReceiver;
import com.ck.taxoteam.taxodriver.service.SendingLocationService;
import com.ck.taxoteam.taxodriver.tools.LocationConverter;
import com.ck.taxoteam.taxodriver.tools.PermitionsHelper;
import com.ck.taxoteam.taxodriver.tools.RouteApi;
import com.ck.taxoteam.taxodriver.tools.RouteResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.ui.IconGenerator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OrderDetailsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnTouchListener, View.OnClickListener, NetworkStateReceiver.NetworkStateReceiverListener {



    private LatLngBounds.Builder routeCameraChanger;
    private Order currentOrder;
    private GoogleMap map;
    private Button actionButton;
    private RouteResponse routeResponse;
    private GoogleApiClient client;
    private RouteApi routeApi;
    private LinearLayout linearLayoutInformation;
    private float lastTouchPosition;
    private float dpScale;
    private ViewGroup.LayoutParams mapParams;
    private SupportMapFragment mapFragment;
    private int displayHeight;
    private DatabaseReference firebaseDatabase;
    private Intent serviceIntent;
    private LatLng currCoords;
    private NetworkStateReceiver networkStateReceiver;
    private Snackbar networkStateSnackbar;
    private LinearLayout parent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        parent = (LinearLayout) findViewById(R.id.details_activity_parent);

        //set back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get Extra
        if(savedInstanceState == null) {
            Bundle data = getIntent().getExtras();
            currentOrder = data.getParcelable("order");
        }
        else{
            currentOrder = savedInstanceState.getParcelable("order");
        }

        showData(currentOrder);

        //Relative layout
        linearLayoutInformation = (LinearLayout) findViewById(R.id.detailsactivity_relative_information);
        linearLayoutInformation.setOnTouchListener(this);

        //Address list
        RecyclerView addressRecyclerView = (RecyclerView) findViewById(R.id.details_address_recycler_view);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this);
        addressRecyclerView.setLayoutManager(manager);
        AddressListAdapter adapter = new AddressListAdapter(this, currentOrder.getToCoords());
        addressRecyclerView.setAdapter(adapter);


        //Map
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_order_detail);
        mapFragment.getMapAsync(this);

        //Google api client (current location)
        if (client == null) {
            client = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        dpScale = getResources().getDisplayMetrics().density;
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        displayHeight = metrics.heightPixels;

        //Action button
        actionButtonInit();

        //database
        firebaseDatabase = FirebaseDatabase.getInstance().getReference();

        //init service intent
        serviceIntent = new Intent(this, SendingLocationService.class);

        //Network state listener
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        networkStateSnackbar = Snackbar.make(parent, getResources().getString(R.string.network_down_snackbar_text),Snackbar.LENGTH_INDEFINITE);
        networkStateSnackbar.setAction(getResources().getText(R.string.action_open_wifi), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));

        //Order change
        firebaseDatabase.child("orders").child(currentOrder.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String oldStatus = currentOrder.getStatus();
                String oldID = currentOrder.getId();
                boolean meAccept = currentOrder.isMeAccept();
                currentOrder = dataSnapshot.getValue(Order.class);
                currentOrder.setId(oldID);
                if (!(currentOrder.getStatus().equals("free")) && oldStatus.equals("free") && !meAccept) {
                    actionButton.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_order_inaccessible), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showData(Order order) {
        TextView fromText = (TextView) findViewById(R.id.textview_details_text_from);
        TextView priceText = (TextView) findViewById(R.id.textview_details_text_price);
        TextView commentText = (TextView) findViewById(R.id.textview_details_text_comment);

        //Set data
        fromText.setText(LocationConverter.getCompleteAddressString(this, order.getFromCoords().
                getLatitude(), order.getFromCoords().getLongitude()));
        priceText.setText(String.valueOf(order.getPrice()) + " " + getResources().getString(R.string.currency_uah));
        if (order.getAdditionalComment() != null) {
            if (!order.getAdditionalComment().equals("")) {
                commentText.setText(order.getAdditionalComment());
            }
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapParams = mapFragment.getView().getLayoutParams();
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(currentOrder.getFromCoords().getLatitude(), currentOrder.getFromCoords().getLongitude()))
                .zoom(15)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.animateCamera(cameraUpdate);
        map.setBuildingsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }
        //Markers
        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setTextAppearance(R.style.markers_text_style);
        String fromAddress;
        if(currentOrder.getFromAddress().equals("")) {
            fromAddress = LocationConverter.getCompleteAddressString(this, currentOrder.getFromCoords().getLatitude(), currentOrder.getFromCoords().getLongitude());
            currentOrder.setFromAddress(fromAddress);
        }
        else {
            fromAddress = currentOrder.getFromAddress();
        }
        iconFactory.setColor(ContextCompat.getColor(this, R.color.markers_green_backgrount));
        map.addMarker(new MarkerOptions().position(new LatLng(currentOrder.getFromCoords().getLatitude(), currentOrder.getFromCoords().getLongitude())).icon(
                BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(getResources().getString(R.string.markers_start_label)))).
                title(fromAddress));

        for (int i = 0; i < currentOrder.getToCoords().size(); i++) {
            Coords c = currentOrder.getToCoords().get(i);
            String address = LocationConverter.getCompleteAddressString(this, c.getLatitude(), c.getLongitude());
            if (i == currentOrder.getToCoords().size() - 1) {
                iconFactory.setColor(ContextCompat.getColor(this, R.color.markers_red_backgrount));
            } else {
                iconFactory.setColor(ContextCompat.getColor(this, R.color.markers_blue_backgrount));
            }
            map.addMarker(new MarkerOptions().position(new LatLng(c.getLatitude(), c.getLongitude())).icon(
                    BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(String.valueOf(i + 1)))
            )
                    .title(address));
        }


        //draw routes
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://maps.googleapis.com").
                addConverterFactory(GsonConverterFactory.create())
                .build();
        routeApi = retrofit.create(RouteApi.class);
        getRoutes(googleMap);

    }

    private void getRoutes(GoogleMap map) {


        //for camera zoom
        routeCameraChanger = new LatLngBounds.Builder();


        LatLng fromPoint = new LatLng(currentOrder.getFromCoords().getLatitude(),
                currentOrder.getFromCoords().getLongitude());

        //From my location to start point


        for (int i = 0; i < currentOrder.getToCoords().size(); i++) {
            LatLng toPoint = new LatLng(currentOrder.getToCoords().get(i).getLatitude(),
                    currentOrder.getToCoords().get(i).getLongitude());

            //default color
            int color = R.color.color_route_default;

            //last point
            if (i == currentOrder.getToCoords().size() - 1) {
                color = R.color.color_route_endpoint;
            }
            getRouteResponse(fromPoint, toPoint, color);
            fromPoint = toPoint;

        }
    }

    private void getRouteResponse(LatLng from, LatLng to, final int color) {
        String fromString = from.latitude + "," + from.longitude;
        String toString = to.latitude + "," + to.longitude;
        Call<RouteResponse> routeCall = routeApi.getRoute(fromString, toString, true, "ru");

        //async
        routeCall.enqueue(new Callback<RouteResponse>() {
            @Override
            public void onResponse(Call<RouteResponse> call, Response<RouteResponse> response) {
                routeResponse = response.body();
                drawRoute(routeResponse.getPoints(), color);
            }

            @Override
            public void onFailure(Call call, Throwable t) {

            }
        });
    }

    private void drawRoute(String points, int LineColor) {
        List<LatLng> listPoints = PolyUtil.decode(points);

        PolylineOptions line = new PolylineOptions();
        line.width(8f);
        line.color(getResources().getColor(LineColor));

        for (LatLng point : listPoints) {
            line.add(point);
            routeCameraChanger.include(point);
        }

        map.addPolyline(line);

        //move camera
        int size = getResources().getDisplayMetrics().widthPixels;
        LatLngBounds latLngBounds = routeCameraChanger.build();
        CameraUpdate track = CameraUpdateFactory.newLatLngBounds(latLngBounds, size, size, 25);
        map.moveCamera(track);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location currLocation;
        if (PermitionsHelper.checkLocationPermitions(this)) {
            currLocation = LocationServices.FusedLocationApi.getLastLocation(client);
            if(currLocation != null) {
                currCoords = new LatLng(currLocation.getLatitude(), currLocation.getLongitude());
                LatLng toCoords = new LatLng(currentOrder.getFromCoords().getLatitude(),
                        currentOrder.getFromCoords().getLongitude());
                getRouteResponse(currCoords, toCoords, R.color.color_route_from_me_to_start_points);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, getResources().getString(R.string.get_routes_error), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        client.connect();
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final float y = event.getRawY();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                lastTouchPosition = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                params.height += lastTouchPosition - y;
                if(params.height < (60 * dpScale)){
                    //set 60 dp - minimum
                    params.height = (int) (60 * dpScale);
                }
                if(params.height > (300*dpScale)){
                    //set 300 dp - maximum
                    params.height = (int) (300 * dpScale);
                }
                lastTouchPosition = y;
                v.setLayoutParams(params);
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch(currentOrder.getStatus()){
            case "free":
                acceptOrder();
                break;
            case "accepted":
                arrivedToStartPosition();
                Toast.makeText(this,getResources().getString(R.string.toast_arrive_start_place),Toast.LENGTH_SHORT).show();
                break;
            case "arrived":
                completeOrder();
                break;
        }
    }
    private void acceptOrder(){
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.accept_dialog_title))
                .setMessage(getResources().getString(R.string.accept_dialog_question))
                .setPositiveButton(getResources().getString(R.string.accept_dialog_positive_button_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        actionButton.setText(R.string.button_text_arrived);
                        currentOrder.setStatus("accepted");

                        //Write driver information to order
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        FirebaseUser currUser = auth.getCurrentUser();
                        currentOrder.setDriverId(currUser.getUid());
                        if(currCoords!=null) {
                            currentOrder.setDriverPos(new Coords(currCoords.longitude, currCoords.latitude));
                        }

                        currentOrder.setMeAccept(true);
                        firebaseDatabase.child("orders").child(currentOrder.getId()).setValue(currentOrder);
                        serviceIntent.putExtra("order", currentOrder);
                        startService(serviceIntent);
                        dialog.cancel();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.accept_dialog_negative_button_text), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }
    private void actionButtonInit(){
        actionButton = (Button) findViewById(R.id.details_button_action);
        actionButton.setOnClickListener(this);
        switch (currentOrder.getStatus()) {
            case "accepted":
                actionButton.setText(R.string.button_text_arrived);
                break;
            case "arrived":
                actionButton.setText(R.string.button_text_completed);
                break;
            case "completed":
                actionButton.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if(MainActivity.isActive()){
                    finish();
                }
                else{
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void arrivedToStartPosition(){
        actionButton.setText(getResources().getString(R.string.button_text_completed));
        firebaseDatabase.child("orders").child(currentOrder.getId()).child("status").setValue("arrived");
        currentOrder.setStatus("arrived");
    }
    private void completeOrder(){
        new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.dialog_order_completed_title))
                .setMessage(getResources().getString(R.string.dialog_order_completed_message))
                .setPositiveButton(getResources().getString(R.string.ok_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
        actionButton.setVisibility(View.INVISIBLE);
        firebaseDatabase.child("orders").child(currentOrder.getId()).child("status").setValue("completed");
        stopService(serviceIntent);
    }

    @Override
    public void networkAvailable() {
        if(networkStateSnackbar.isShown()) {
            networkStateSnackbar.dismiss();
        }
    }

    @Override
    public void networkUnavailable() {
        networkStateSnackbar.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("order", currentOrder);
        super.onSaveInstanceState(outState);
    }

}
