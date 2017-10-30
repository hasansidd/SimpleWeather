package com.siddapps.android.simpleweather.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.siddapps.android.simpleweather.R;
import com.siddapps.android.simpleweather.data.Weather;
import com.siddapps.android.simpleweather.data.WeatherStation;

public class WeatherView extends LinearLayout {
    private TextView mCityNameText;
    private TextView mCurrentTempText;
    private TextView mHighTemp;
    private TextView mLowTemp;
    private TextView mCurrentDescriptionText;
    private ImageView mWeatherBackgroundImage;
    private ImageView mWeatherAlertImage;
    private TextView mTimeText;

    public WeatherView(Context context) {
        super(context);
        initializeViews(context);
    }

    public WeatherView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public WeatherView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeViews(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public WeatherView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.weather_view, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mCityNameText = findViewById(R.id.city_name);
        mCurrentTempText = findViewById(R.id.current_temp_text);
        mHighTemp = findViewById(R.id.temp_high_text);
        mLowTemp = findViewById(R.id.temp_low_text);
        mCurrentDescriptionText = findViewById(R.id.weather_description_text);
        mWeatherBackgroundImage = findViewById(R.id.weather_background_image);
        mTimeText = findViewById(R.id.weather_time_text);
        mWeatherAlertImage = findViewById(R.id.weather_alert);
    }

    public void bindWeather(Weather weather) {
        WeatherStation mWeatherStation = WeatherStation.get();
        mCityNameText.setText(weather.getName());
        mCurrentTempText.setText(mWeatherStation.formatTemp(getContext(), weather.getTemp()));
        mHighTemp.setText(mWeatherStation.formatTemp(getContext(), weather.getTemp_max()));
        mLowTemp.setText(mWeatherStation.formatTemp(getContext(), weather.getTemp_min()));
        mCurrentDescriptionText.setText(weather.getDetailedDescription());
        mWeatherBackgroundImage.setImageResource(weather.getIcon());
        mTimeText.setText(weather.getTime());
        //noinspection ResourceType
        mWeatherAlertImage.setVisibility(weather.getNotifyAlert());
    }
}
