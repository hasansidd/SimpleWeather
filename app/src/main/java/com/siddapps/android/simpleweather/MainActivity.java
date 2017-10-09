package com.siddapps.android.simpleweather;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobManager;
import com.siddapps.android.simpleweather.WeatherJobs.WeatherFetchJob;
import com.siddapps.android.simpleweather.WeatherJobs.WeatherJobCreator;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {
    static public WeatherFetcher mWeatherFetcher;
    static public String TEMPERATURE_SETTING = "F";
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
        Fabric.with(this, new Crashlytics());
        JobManager.create(this).addJobCreator(new WeatherJobCreator());
        WeatherFetchJob.scheduleJob();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mWeatherFetcher = new WeatherFetcher(this);
        } else {
            Intent i = WeatherActivity.newIntent(this);
            startActivity(i);
        }
    }

    public void changeTemp() {
        WeatherStation weatherStation = WeatherStation.get(this);
        TEMPERATURE_SETTING = weatherStation.getTempSetting();
    }

}
