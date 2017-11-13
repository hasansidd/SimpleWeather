package com.siddapps.android.simpleweather.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.util.Log;
import android.view.View;

import com.siddapps.android.simpleweather.R;
import com.siddapps.android.simpleweather.data.WeatherFetcher;

import static com.siddapps.android.simpleweather.util.TimeUtil.formatTime;

@Entity(tableName = "weather")
public class Weather {
    private static final String TAG = "Weather";
    @PrimaryKey(autoGenerate = true)
    public int id;
    private String name;
    private String temp;
    private String temp_min;
    private String temp_max;
    private String mainDescription;
    private String detailedDescription;
    private String lat;
    private String lon;
    private long sunrise;
    private long sunset;
    private long time;
    private String zipCode;
    private int icon;
    private ExtendedForecast mExtendedForecast;
    private String source;
    private long timeFetched;
    private boolean notifyReady;

    public Weather(int id, String name, String temp, String temp_max, String temp_min, String mainDescription, String detailedDescription,
                   String lat, String lon, long sunrise, long sunset, long time, String zipCode, int icon, String source, long timeFetched, boolean notifyReady) {
        this.id = id;
        this.name = name;
        this.temp = temp;
        this.temp_max = temp_max;
        this.temp_min = temp_min;
        this.mainDescription = mainDescription;
        this.detailedDescription = detailedDescription;
        this.lat = lat;
        this.lon = lon;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.time = time;
        this.zipCode = zipCode;
        this.icon = icon;
        this.source = source;
        this.timeFetched = timeFetched;
        this.notifyReady = notifyReady;
    }

    @Ignore
    public Weather() {
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

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLatLon() {
        return String.format("%s,%s", lat, lon);
    }

    public String getSunrise() {
        return formatTime(sunrise);
    }

    public void setSunrise(long sunrise) {
        this.sunrise = sunrise;
    }

    public String getSunset() {
        return formatTime(sunset);
    }

    public void setSunset(long sunset) {
        this.sunset = sunset;
    }

    public String getTime() {
        return formatTime(time);
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public int getIcon() {
        if (time > sunset || time < sunrise) {//night
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
            }
        }
        switch (getMainDescription()) {//day
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
                Log.i(TAG, "Description: " + mainDescription + " not found");
                return R.drawable.clear;
        }
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public ExtendedForecast getExtendedForecast() {
        return mExtendedForecast;
    }

    public void setExtendedForecast(ExtendedForecast extendedForecast) {
        mExtendedForecast = extendedForecast;
    }

    public String getSource() {
        switch (source) {
            case WeatherFetcher.SOURCE_ZIP:
                return zipCode;
            case WeatherFetcher.SOURCE_LATLON:
                return getLatLon();
            case WeatherFetcher.SOURCE_CITY:
            default:
                return name;
        }
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Long getTimeFetched() {
        return timeFetched;
    }

    public void setTimeFetched(Long timeFetched) {
        this.timeFetched = timeFetched;
    }

    public int getNotifyAlert() {
        if (mExtendedForecast != null) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    public boolean isNotifyReady() {
        return notifyReady;
    }

    public void setNotifyReady(boolean notifyReady) {
        this.notifyReady = notifyReady;
    }
}
