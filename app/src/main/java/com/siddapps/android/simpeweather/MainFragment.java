package com.siddapps.android.simpeweather;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";
    private WeatherFetcher mWeatherFetcher;
    private Weather mWeather;
    private TextView mCityName;
    private TextView mTemp;
    private TextView mDescription;
    private LocationUtil mLocationUtil;

    private void updateUI() {
        mCityName.setText(mWeather.getName());
        mTemp.setText(mWeather.getTemp());
        mDescription.setText(mWeather.getDescription());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        mCityName = (TextView) v.findViewById(R.id.city_name_text);
        mTemp = (TextView) v.findViewById(R.id.current_temp);
        mDescription = (TextView) v.findViewById(R.id.description_text);

        mWeatherFetcher = new WeatherFetcher();
        mLocationUtil = new LocationUtil(getActivity());

        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
        fetchWeatherTask.execute();

        return v;
    }

    public class FetchWeatherTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {
                String zipCode = mLocationUtil.getCurrentLocationZip();
                mWeather = mWeatherFetcher.getCurrentWeather(zipCode);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateUI();
        }
    }

}
