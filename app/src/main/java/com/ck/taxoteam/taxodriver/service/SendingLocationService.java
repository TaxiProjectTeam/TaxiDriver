package com.ck.taxoteam.taxodriver.service;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
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

public class SendingLocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    FirebaseDatabase database;
    DatabaseReference ref;
    Order currOrder;
    GoogleApiClient client;
    Location currLocation;
    NotificationManager notificationManager;
    private boolean isRunning = true;
    private Thread thread;
    private Notification notification;

    public SendingLocationService() {
        super();
    }

    @Override
    public void onCreate() {
        database = FirebaseDatabase.getInstance();
        ref = database.getReference();
        //init google api client
        if (client == null) {
            client = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        client.connect();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        currOrder = intent.getParcelableExtra("order");
        initNotification();
        startForeground(1,notification);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    while (isRunning) {
                        synchronized (this) {
                            if(currLocation != null) {
                                currLocation = LocationServices.FusedLocationApi.getLastLocation(client);
                                Coords currCoords = new Coords(currLocation.getLongitude(), currLocation.getLatitude());
                                ref.child("orders").child(currOrder.getId()).child("driverPos").setValue(currCoords);
                            }
                            wait(6000);
                        }
                    }
                } catch (Exception e) {
                    stopSelf();
                }
            }
        };

        thread = new Thread(runnable);
        thread.start();
        return START_NOT_STICKY;
    }


    private void initNotification(){
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this);
        Intent notificationIntent = new Intent(this, OrderDetailsActivity.class);
        notificationIntent.putExtra("order", currOrder);
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.mipmap.icon_launcher);
        builder.setContentTitle(getResources().getString(R.string.notification_title));
        builder.setContentText(getResources().getString(R.string.notification_text));
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setPriority(1);
        builder.setDefaults(Notification.DEFAULT_ALL);

        notification = builder.build();
    }


    @Override
    public void onDestroy() {
        isRunning = false;
        stopForeground(true);
        notificationManager.cancelAll();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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
}
