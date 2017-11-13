package com.siddapps.android.simpleweather.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.siddapps.android.simpleweather.R;

import static com.siddapps.android.simpleweather.util.TimeUtil.formatTime;

@Entity
public class HourlyData {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String temp;
    private String temp_min;
    private String temp_max;
    private String mainDescription;
    private String detailedDescription;
    private long time;
    private int icon;
    private String night;

    @Ignore
    public HourlyData() {
    }

    public HourlyData(int id, String name, String temp, String temp_max, String temp_min, String mainDescription, String detailedDescription, long time, int icon, String night) {
        this.id = id;
        this.name = name;
        this.temp = temp;
        this.temp_max = temp_max;
        this.temp_min = temp_min;
        this.mainDescription = mainDescription;
        this.detailedDescription = detailedDescription;
        this.time = time;
        this.icon = icon;
        this.night = night;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getTemp_min() {
        return temp_min;
    }

    public void setTemp_min(String temp_min) {
        this.temp_min = temp_min;
    }

    public String getTemp_max() {
        return temp_max;
    }

    public void setTemp_max(String temp_max) {
        this.temp_max = temp_max;
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

    public String getTime() {
        return formatTime(time);
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getIcon() {
        if (night.contains("n")) {//night
            switch (getMainDescription()) {
                case "Haze":
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
                default:
                    return R.drawable.clearn;
            }
        }
        switch (mainDescription) {
            case "Haze":
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
                return R.drawable.clear;
        }
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getNight() {
        return night;
    }

    public void setNight(String night) {
        this.night = night;
    }










}
