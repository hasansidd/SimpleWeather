package com.siddapps.android.simpeweather;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.List;
import java.util.Locale;

public class LocationUtil {
    private static final String TAG = "LocationUtil";
    private LocationManager mLocationManager;
    private Location lastKnownLocation;
    private Context mContext;

    public LocationUtil(Context context) {
        mContext = context;
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private boolean isLocationGranted() {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    public String getCurrentLocationZip() throws Exception {
        if (isLocationGranted()) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Location permissions not granted");
            }
            lastKnownLocation = getLastKnownLocation();

            Geocoder geocoder = new Geocoder(mContext.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), 1);

            if (addresses.get(0).getPostalCode() != null) {
                String zipCode = addresses.get(0).getPostalCode();
                return zipCode;
            } else {
                Log.i(TAG, "Location permissions not granted");
            }
        }
        return null;
    }

    private Location getLastKnownLocation() {
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Location permissions not granted");
            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            Log.i(TAG, String.format("last known location, provider: %s, location: %s", provider, l));

            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                Log.i(TAG, "found best last known location: " + l);
                bestLocation = l;
            }
        }
        if (bestLocation == null) {
            Log.i(TAG, "Last known location not available from any provider");
        }
        return bestLocation;
    }
}
