package com.ck.taxoteam.taxodriver.tools;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.util.List;
import java.util.Locale;

/**
 * Created by bogdan on 16.02.17.
 */

public class LocationConverter {

    public static String getCompleteAddressString(Context context, double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                if (returnedAddress.getSubThoroughfare() == null) {
                    strAdd = returnedAddress.getThoroughfare();
                } else {
                    strAdd = returnedAddress.getThoroughfare() + ", " + returnedAddress.getSubThoroughfare();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strAdd;
    }
}
