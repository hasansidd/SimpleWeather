package com.siddapps.android.simpleweather.weatherdetail;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.siddapps.android.simpleweather.data.WeatherStation;
import com.siddapps.android.simpleweather.util.SingleFragmentActivity;

public class WeatherDetailActivity extends SingleFragmentActivity {
    private static final String TAG = "WeatherDetailActivity";

    public static Intent newIntent(Context context, int id) {
        Intent intent = new Intent(context, WeatherDetailActivity.class);
        intent.putExtra(WeatherDetailFragment.EXTRA_CITY_ID, id);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        int id = getIntent().getIntExtra(WeatherDetailFragment.EXTRA_CITY_ID,-1);
        return WeatherDetailFragment.newInstance(id);
    }

}
