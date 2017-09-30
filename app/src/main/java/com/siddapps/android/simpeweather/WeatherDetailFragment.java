package com.siddapps.android.simpeweather;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class WeatherDetailFragment extends Fragment {
    static Weather mWeather;
    private TextView mCityNameText;
    private TextView mCurrentTempText;
    private TextView mHighTemp;
    private TextView mLowTemp;
    private TextView mDescriptionText;
    private TextView mCurrentTimeText;
    private TextView mSunrise;
    private TextView mSunset;
    private ImageView mWeatherImage;

    public static WeatherDetailFragment newInstance() {
        return new WeatherDetailFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_weather_detail,container,false);
        mCityNameText = (TextView) v.findViewById(R.id.master_city_name);
        mCurrentTempText = (TextView) v.findViewById(R.id.master_temp_text);
        mDescriptionText = (TextView) v.findViewById(R.id.master_description_text);
        mCurrentTimeText = (TextView) v.findViewById(R.id.master_time_text);
        mWeatherImage = (ImageView) v.findViewById(R.id.master_background_image);

        updateUI();
        return v;
    }

    private void updateUI() {
        mCityNameText.setText(mWeather.getName());
        mCurrentTempText.setText(mWeather.getTemp());
        mDescriptionText.setText(mWeather.getDetailedDescription());
        mWeatherImage.setImageResource(mWeather.getIcon());
        mCurrentTimeText.setText(mWeather.formatTime(mWeather.getTime()));
    }
}
