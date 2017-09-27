package com.siddapps.android.simpeweather;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class MainFragment extends Fragment {
    private WeatherFetcher mWeatherFetcher;
    private TextView mCityName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        mCityName = (TextView) v.findViewById(R.id.city_name_text);
        mWeatherFetcher = new WeatherFetcher();
        FetchWeather fetchWeather = new FetchWeather();
        fetchWeather.execute();
        return v;
    }

    public class FetchWeather extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                mWeatherFetcher.getCurrentWeather("71111");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
