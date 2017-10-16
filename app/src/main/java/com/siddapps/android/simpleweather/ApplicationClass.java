package com.siddapps.android.simpleweather;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobManager;
import com.siddapps.android.simpleweather.weatherjobs.WeatherJobCreator;

import io.fabric.sdk.android.Fabric;

public class ApplicationClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JobManager.create(this).addJobCreator(new WeatherJobCreator());
        Fabric.with(this, new Crashlytics());
    }
}
