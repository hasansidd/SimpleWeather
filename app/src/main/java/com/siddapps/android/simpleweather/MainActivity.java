package com.siddapps.android.simpleweather;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.siddapps.android.simpleweather.data.WeatherFetcher;
import com.siddapps.android.simpleweather.data.WeatherStation;
import com.siddapps.android.simpleweather.util.LocationUtil;
import com.siddapps.android.simpleweather.weather.WeatherActivity;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Intent i = WeatherActivity.newIntent(this);
        startActivity(i);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            LocationUtil locationUtil = new LocationUtil(this);
        } else {
            Intent i = WeatherActivity.newIntent(this);
            startActivity(i);
        }
    }
}
