package com.siddapps.android.simpeweather;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.List;
import java.util.Locale;

public class LocationUtil {
    private static final String TAG = "LocationUtil";
    private LocationManager locationManager;
    private Location lastKnownLocation;
    private Context mContext;
    private boolean isGranted = false;

    public LocationUtil(Context context) {
        mContext = context;
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
    }

    public boolean locationGranted(){
        Log.i(TAG, "requested");
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    public String getCurrentLocationZip() throws Exception {
        Geocoder geocoder = new Geocoder(mContext.getApplicationContext(), Locale.getDefault());
        List<Address> addresses=geocoder.getFromLocation(lastKnownLocation.getLatitude(),lastKnownLocation.getLongitude(),1);

        Log.i(TAG, "here");
        if (addresses.get(0).getPostalCode() != null){
            String zipCode=addresses.get(0).getPostalCode();
            return zipCode;
        }
        return null;
    }


}
