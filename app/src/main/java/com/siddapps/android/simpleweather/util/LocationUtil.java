package com.siddapps.android.simpleweather.util;

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

    public Location getCurrentLocationLatLon() throws Exception {
        if (isLocationGranted()) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Location permissions not granted");
            }
            return getLastKnownLocation();
        }
        return null;
    }

    private Location getLastKnownLocation() {
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Location permissions not granted");
        }

        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            //Log.i(TAG, String.format("Location provider : %s, accuracy: %s", provider, l.getAccuracy()));

            if (l == null) {
                continue;
            }

            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                if (bestLocation == null) {
                    Log.i(TAG, String.format("Accuracy from %s : %.2f", l.getProvider(), l.getAccuracy()));
                } else {
                    Log.i(TAG, String.format("Accuracy from %s : %.2f > Accuracy from %s : %.2f", l.getProvider(), l.getAccuracy(), bestLocation.getProvider(), bestLocation.getAccuracy()));
                }
                bestLocation = l;
            }
        }
        if (bestLocation == null) {
            Log.i(TAG, "Last known location not available from any provider");
        }
        return bestLocation;
    }
}
