package com.siddapps.android.simpeweather;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WeatherStation {
    private static final String TAG = "WeatherStation";
    private List<Weather> mWeathers;
    private static WeatherStation sWeatherStation;
    private Context mContext;

    public static WeatherStation get(Context context) {
        if (sWeatherStation == null) {
            sWeatherStation = new WeatherStation(context);
        }
        return sWeatherStation;
    }

    private WeatherStation(Context context) {
        mContext = context.getApplicationContext();
        mWeathers = new ArrayList<>();
    }

    public List<Weather> getWeathers() {
        return mWeathers;
    }

    public void addWeather(Weather weather) {
        if (!mWeathers.isEmpty()) {
            for (int i = 0; i < mWeathers.size(); i++) {
                if (mWeathers.get(i).getName().contains(weather.getName())) {
                    Log.i(TAG, "City: " + weather.getName() + " already exists\nmoving to beginning of list");
                    mWeathers.remove(i);
                    mWeathers.add(0, weather);
                    return;
                }
            }
        }
        Log.i(TAG, "City: " + weather.getName() + " added to end of list");
        mWeathers.add(weather);
    }

    public void addCurrentWeather(Weather weather) {
        if (weather == null) {
            Log.i(TAG, "addCurrentWeather weather is null");
            return;
        }

        Log.i(TAG, "started: " + mWeathers.size());

        if (!mWeathers.isEmpty()) {
            for (int i = 0; i < mWeathers.size(); i++) {
                if (mWeathers.get(i).getName().contains(weather.getName())) {
                    Log.i(TAG, "City: " + weather.getName() + " already exists, skipping");
                    return;
                }
            }
        }
        Log.i(TAG, "City: " + weather.getName() + " added to beginning of list");
        mWeathers.add(0, weather);
    }

    public void deleteWeather(Weather weather) {
        mWeathers.remove(weather);
    }
}
