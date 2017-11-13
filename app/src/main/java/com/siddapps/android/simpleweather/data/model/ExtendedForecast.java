package com.siddapps.android.simpleweather.data.model;

import java.util.ArrayList;
import java.util.List;


public class ExtendedForecast {
    private List<HourlyData> mHourlyDataList;

    public ExtendedForecast() {
        if (mHourlyDataList == null) {
            mHourlyDataList = new ArrayList<>();
        }
    }

    public List<HourlyData> getHourlyDataList() {
        return mHourlyDataList;
    }

    public void addHourlyData(HourlyData hourlyData) {
        mHourlyDataList.add(hourlyData);
    }

}