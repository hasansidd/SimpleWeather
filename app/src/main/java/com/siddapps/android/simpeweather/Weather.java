package com.siddapps.android.simpeweather;

import android.util.Log;

public class Weather {
    private static final String TAG = "Weather";
    private String lon;
    private String lat;
    private String main;
    private String description;
    private String humidity;
    private String temp;
    private String temp_min;
    private String temp_max;
    private String name;
    private String id;

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

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        tempDouble = tempDouble*(9/5d) - 459.67; //Fahrenheit
        this.temp = String.format("%.0f", tempDouble);
    }

    public String getTemp_min() {
        return temp_min;
    }

    public void setTemp_min(String temp_min) {
        Double temp_minDouble = Double.parseDouble(temp_min);
        temp_minDouble = temp_minDouble*(9/5d) - 459.67; //Fahrenheit
        this.temp_min = String.format("%.0f", temp_minDouble);
    }

    public String getTemp_max() {
        return temp_max;
    }

    public void setTemp_max(String temp_max) {
        Double temp_maxDouble = Double.parseDouble(temp_max);
        temp_maxDouble = temp_maxDouble*(9/5d) - 459.67; //Fahrenheit
        this.temp_max = String.format("%.0f", temp_maxDouble);
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
