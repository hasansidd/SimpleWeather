package com.siddapps.android.simpleweather.WeatherJobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class WeatherJobCreator implements JobCreator {

    @Override @Nullable
    public Job create(@NonNull String tag) {
        switch (tag) {
            case WeatherFetchJob.TAG:
                return new WeatherFetchJob();
            default:
                return null;
        }
    }
}
