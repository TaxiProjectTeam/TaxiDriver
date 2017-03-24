package com.ck.taxoteam.taxodriver.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ck.taxoteam.taxodriver.R;
import com.ck.taxoteam.taxodriver.adapter.AddressListAdapter;
import com.ck.taxoteam.taxodriver.data.Coords;
import com.ck.taxoteam.taxodriver.data.Order;
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
import com.google.maps.android.PolyUtil;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OrderDetailsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnTouchListener {



    private LatLngBounds.Builder routeCameraChanger;
    private Order currentOrder;
    private GoogleMap map;
    private RouteResponse routeResponse;
    private GoogleApiClient client;
    private RouteApi routeApi;
    private LinearLayout linearLayoutInformation;
    private float lastTouchPosition;
    private float dpScale;
    ViewGroup.LayoutParams mapParams;
    private SupportMapFragment mapFragment;
    private int displayHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        //Get Extra
        Bundle data = getIntent().getExtras();
        currentOrder = data.getParcelable("order");
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
        String fromAddress = LocationConverter.getCompleteAddressString(this, currentOrder.getFromCoords().getLatitude(), currentOrder.getFromCoords().getLongitude());

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


        List<LatLng> allPoints = new ArrayList<LatLng>();
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
            LatLng currCoords = new LatLng(currLocation.getLatitude(), currLocation.getLongitude());
            LatLng toCoords = new LatLng(currentOrder.getFromCoords().getLatitude(),
                    currentOrder.getFromCoords().getLongitude());
            getRouteResponse(currCoords, toCoords, R.color.color_route_from_me_to_start_points);
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
        //mapParams.height = (int) (displayHeight - params.height
          //      -56 * dpScale);         //Toolbar size
        return true;
    }
}
