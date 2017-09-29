package com.siddapps.android.simpeweather;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Weather {
    private static final String TAG = "Weather";
    private String lon;
    private String lat;
    private String mainDescription;
    private String detailedDescription;
    private String humidity;
    private String temp;
    private String temp_min;
    private String temp_max;
    private String name;
    private String id;
    private int icon;
    private long time;
    private long sunrise;
    private long sunset;

    public String formatTime(long millis) {
        Date date = new Date(millis * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE hh:mm a");
        return sdf.format(date);
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getSunrise() {
        return sunrise;
    }

    public void setSunrise(long sunrise) {
        this.sunrise = sunrise;
    }

    public long getSunset() {
        return sunset;
    }

    public void setSunset(long sunset) {
        this.sunset = sunset;
    }

    public int getIcon() {
        if (time > sunset || time < sunrise) {//night
            Log.i(TAG, "in");
            switch (getMainDescription()) {
                case "Clouds":
                    return R.drawable.cloudyn;
                case "Clear":
                    return R.drawable.clearn;
                case "Snow":
                    return R.drawable.snowyn;
                case "Rain":
                case "Drizzle":
                case "Mist":
                    return R.drawable.rainyn;
                case "Thunderstorm":
                    return R.drawable.stormyn;
            }
        }
        switch (getMainDescription()) {//day
            case "Clouds":
                return R.drawable.cloudy;
            case "Clear":
                return R.drawable.clear;
            case "Snow":
                return R.drawable.snowy;
            case "Rain":
            case "Drizzle":
            case "Mist":
                return R.drawable.rainy;
            case "Thunderstorm":
                return R.drawable.stormy;
            default:
                Log.i(TAG, "Description: " + mainDescription + " not found");
                return R.drawable.clear;
        }
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getMainDescription() {
        return mainDescription;
    }

    public void setMainDescription(String mainDescription) {
        this.mainDescription = mainDescription;
    }

    public String getDetailedDescription() {
        return detailedDescription;
    }

    public void setDetailedDescription(String detailedDescription) {
        this.detailedDescription = detailedDescription;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        Double tempDouble = Double.parseDouble(temp);
        tempDouble = tempDouble * (9 / 5d) - 459.67; //Fahrenheit
        this.temp = String.format("%.0f°F", tempDouble);
    }

    public String getTemp_min() {
        return temp_min;
    }

    public void setTemp_min(String temp_min) {
        Double temp_minDouble = Double.parseDouble(temp_min);
        temp_minDouble = temp_minDouble * (9 / 5d) - 459.67; //Fahrenheit
        this.temp_min = String.format("%.0f°F", temp_minDouble);
    }

    public String getTemp_max() {
        return temp_max;
    }

    public void setTemp_max(String temp_max) {
        Double temp_maxDouble = Double.parseDouble(temp_max);
        temp_maxDouble = temp_maxDouble * (9 / 5d) - 459.67; //Fahrenheit
        this.temp_max = String.format("%.0f°F", temp_maxDouble);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
