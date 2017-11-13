package com.siddapps.android.simpleweather.weatherdetail;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import com.siddapps.android.simpleweather.util.SingleFragmentActivity;

public class WeatherDetailActivity extends SingleFragmentActivity {
    private static final String TAG = "WeatherDetailActivity";

    public static Intent newIntent(Context context, String cityName) {
        Intent intent = new Intent(context, WeatherDetailActivity.class);
        intent.putExtra(WeatherDetailFragment.EXTRA_CITY_NAME, cityName);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        String cityName = getIntent().getStringExtra(WeatherDetailFragment.EXTRA_CITY_NAME);
        return WeatherDetailFragment.newInstance(cityName);
    }

}
