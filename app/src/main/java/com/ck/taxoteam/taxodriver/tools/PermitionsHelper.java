package com.ck.taxoteam.taxodriver.tools;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by bogdan on 22.03.17.
 */

public class PermitionsHelper {
    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 13;

    public static boolean checkLocationPermitions(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
        }
        try {
            return ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        catch (Exception e){
            return false;
        }
    }
}
