package com.example.sveta.taxodriver.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.example.sveta.taxodriver.R;
import com.example.sveta.taxodriver.adapter.AddressListAdapter;
import com.example.sveta.taxodriver.data.Coords;
import com.example.sveta.taxodriver.data.Order;
import com.example.sveta.taxodriver.tools.LocationConverter;
import com.example.sveta.taxodriver.tools.RouteApi;
import com.example.sveta.taxodriver.tools.RouteResponse;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OrderDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    LatLngBounds.Builder routeCameraChanger;
    private Order currentOrder;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private RecyclerView addressRecyclerView;
    private RecyclerView.LayoutManager manager;
    private RouteResponse routeResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        //Get Extra
        Bundle data = getIntent().getExtras();
        currentOrder = data.getParcelable("order");
        showData(currentOrder);


        //Address list
        addressRecyclerView = (RecyclerView) findViewById(R.id.details_address_recycler_view);
        manager = new LinearLayoutManager(this);
        addressRecyclerView.setLayoutManager(manager);
        AddressListAdapter adapter = new AddressListAdapter(this, currentOrder.getToCoords());
        addressRecyclerView.setAdapter(adapter);


        //Map
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_order_detail);
        mapFragment.getMapAsync(this);
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
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(currentOrder.getFromCoords().getLatitude(), currentOrder.getFromCoords().getLongitude()))
                .zoom(15)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.animateCamera(cameraUpdate);
        map.setBuildingsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }
        //Markers
        String fromAddress = LocationConverter.getCompleteAddressString(this, currentOrder.getFromCoords().getLatitude(), currentOrder.getFromCoords().getLongitude());
        map.addMarker(new MarkerOptions().position(new LatLng(currentOrder.getFromCoords().getLatitude(), currentOrder.getFromCoords().getLongitude())).icon(
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)).title(fromAddress));

        for (int i = 0; i < currentOrder.getToCoords().size(); i++) {
            Coords c = currentOrder.getToCoords().get(i);
            String address = LocationConverter.getCompleteAddressString(this, c.getLatitude(), c.getLongitude());
            if (i == currentOrder.getToCoords().size() - 1) {
                map.addMarker(new MarkerOptions().position(new LatLng(c.getLatitude(), c.getLongitude())).title(address));
                break;
            }
            map.addMarker(new MarkerOptions().position(new LatLng(c.getLatitude(), c.getLongitude())).icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            )
                    .title(address));
        }

        getRoutes(googleMap);

    }

    private void getRoutes(GoogleMap map) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://maps.googleapis.com").
                addConverterFactory(GsonConverterFactory.create())
                .build();
        RouteApi routeApi = retrofit.create(RouteApi.class);

        //for camera zoom
        routeCameraChanger = new LatLngBounds.Builder();

        List<LatLng> allPoints = new ArrayList<LatLng>();
        LatLng fromPoint = new LatLng(currentOrder.getFromCoords().getLatitude(),
                currentOrder.getFromCoords().getLongitude());
        for(Coords c : currentOrder.getToCoords()){
            //TODO: colors and from current position to start position

            LatLng toPoint = new LatLng(c.getLatitude(),c.getLongitude());
            getRouteResponse(routeApi, fromPoint.latitude + "," + fromPoint.longitude
                    , toPoint.latitude + "," + toPoint.longitude, R.color.md_black_1000);

            fromPoint = toPoint;
        }
    }

    private void getRouteResponse(RouteApi api, String from, String to, final int color) {
        Call<RouteResponse> routeCall = api.getRoute(from, to, true, "ru");

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
}
