package com.ck.taxoteam.taxodriver.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ck.taxoteam.taxodriver.R;
import com.ck.taxoteam.taxodriver.activity.OrderDetailsActivity;
import com.ck.taxoteam.taxodriver.data.Coords;
import com.ck.taxoteam.taxodriver.data.Order;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class SendLocationService extends IntentService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    Order currentOrder;
    DatabaseReference ref;
    FirebaseDatabase database;
    GoogleApiClient client;
    Location currLocation;
    private NotificationManager nm;

    public SendLocationService(String name) {
        super(name);
    }
    public SendLocationService(){
        super("SendLocationServeice");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference();
        if (client == null) {
            client = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        client.connect();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        currentOrder = intent.getParcelableExtra("order");
        showNotification();
        while(true){
            synchronized (this){
                try {
                    currLocation = LocationServices.FusedLocationApi.getLastLocation(client);
                    if(currLocation != null){
                        Coords currCoords = new Coords(currLocation.getLongitude(),currLocation.getLatitude());
                        ref.child("orders").child(currentOrder.getId()).child("driverPos").setValue(currCoords);
                    }
                    wait(6000);
                }
                catch (Exception e){
                    stopSelf();
                }
            }
        }
    }
    private void showNotification(){
        nm = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this);
        Intent notificationIntent = new Intent(this, OrderDetailsActivity.class);
        notificationIntent.putExtra("order", currentOrder);
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.mipmap.icon_launcher);
        builder.setContentTitle(getResources().getString(R.string.notification_title));
        builder.setContentText(getResources().getString(R.string.notification_text));
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setDefaults(Notification.DEFAULT_ALL);

        Notification notification = builder.build();
        nm.notify((int)System.currentTimeMillis(),notification);
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        currLocation = LocationServices.FusedLocationApi.getLastLocation(client);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        nm.cancelAll();
    }
}
