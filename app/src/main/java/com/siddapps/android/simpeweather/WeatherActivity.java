package com.siddapps.android.simpeweather;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;


public class WeatherActivity extends SingleFragmentActivity {
    private static final String TAG = "WeatherActivity";

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, WeatherActivity.class);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        return new MainFragment();
    }
}
