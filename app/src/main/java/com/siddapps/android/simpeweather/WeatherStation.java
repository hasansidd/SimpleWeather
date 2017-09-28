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

        //mWeathers.add(weather);

        for(int i = 0; i < mWeathers.size(); i++)
        {
            if(mWeathers.get(i).getName().contains(weather.getName())) {
               // mWeathers.remove(i);
               // mWeathers.add(0,weather);
                Log.i(TAG, "TODO");
            } else {
                mWeathers.add(weather);
                break;
            }
        }
    }

    public void addCurrentWeather(Weather weather) {
        Log.i(TAG, "started: " + mWeathers.size());
        if (mWeathers.isEmpty()) {
            mWeathers.add(0, weather);
        }

        for(int i = 0; i < mWeathers.size(); i++)
        {
            if(mWeathers.get(i).getName().contains(weather.getName())) {
                Log.i(TAG, "true");
            } else {
                Log.i(TAG, "false");
                mWeathers.add(0, weather);
            }
            Log.i(TAG, String.valueOf(i));
        }
    }

    public void deleteWeather(Weather weather) {
        mWeathers.remove(weather);
    }
}
