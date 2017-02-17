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
import com.example.sveta.taxodriver.data.Order;
import com.example.sveta.taxodriver.tools.LocationConverter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class OrderDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Order currentOrder;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private RecyclerView addressRecyclerView;
    private RecyclerView.LayoutManager manager;

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
        fromText.setText(LocationConverter.getCompleteAddressString(this, order.getFromCoords().getLatitude(), order.getFromCoords().getLongitude()));
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
                .zoom(10)
                .bearing(45)
                .tilt(20)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.animateCamera(cameraUpdate);
        map.setBuildingsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }

        //TODO: draw route
    }
}
