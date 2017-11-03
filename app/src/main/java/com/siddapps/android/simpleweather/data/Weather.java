package com.siddapps.android.simpleweather.data;

import android.util.Log;
import android.view.View;

import com.siddapps.android.simpleweather.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Weather {
    private static final String TAG = "Weather";
    private String mainDescription;
    private String detailedDescription;
    private String humidity;
    private String temp;
    private String temp_min;
    private String temp_max;
    private String name;
    private String lat;
    private String lon;
    private int icon;
    private long time;
    private long sunrise;
    private long sunset;
    private ExtendedForecast mExtendedForecast;
    private boolean isExtendedForecastReady;
    private String sourceType;
    private Long timeFetched;

    public Long getTimeFetched() {
        return timeFetched;
    }

    public void setTimeFetched(Long timeFetched) {
        this.timeFetched = timeFetched;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    private String zipCode;

    public String getSource() {
        switch (sourceType) {
            case WeatherFetcher.SOURCE_ZIP:
                return zipCode;
            case WeatherFetcher.SOURCE_LATLON:
                return getLatLon();
            case WeatherFetcher.SOURCE_CITY:
            default:
                return name;
        }
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
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

    public int getNotifyAlert() {
        if (mExtendedForecast!=null && mExtendedForecast.isNotifyReady()){
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    public boolean isExtendedForecastReady() {
        return isExtendedForecastReady;
    }

    public void setExtendedForecastReady(boolean extendedForecastReady) {
        isExtendedForecastReady = extendedForecastReady;
    }

    public static String formatTime(long millis) {
        Date date = new Date(millis * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE hh:mm a");
        return sdf.format(date);
    }

    public ExtendedForecast getExtendedForecast() {
        if (mExtendedForecast == null) {
            mExtendedForecast = new ExtendedForecast();
        }
        return mExtendedForecast;
    }

    public void setExtendedForecast(ExtendedForecast extendedForecast) {
        mExtendedForecast = extendedForecast;
        setExtendedForecastReady(true);
    }

    public String getTime() {
        return formatTime(time);
    }

    public void setTime(long time) {
        this.time = time;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static class ExtendedForecast {
        private List<HourlyData> mHourlyDataList;

        public boolean isNotifyReady() {
            return isNotifyReady;
        }

        public void setNotifyReady(boolean notifyReady) {
            isNotifyReady = notifyReady;
        }

        private boolean isNotifyReady;

        private ExtendedForecast() {
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

        public static class HourlyData {
            private String mTemp;
            private String mTemp_min;
            private String mTemp_max;
            private String mHumidity;
            private long mTime;
            private String mainDescription;
            private String detailedDescription;
            private int icon;
            private String night;

            public void setNight(String night) {
                this.night = night;
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
                            Log.i(TAG, "Description: " + mainDescription + " not found");
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
                        Log.i(TAG, "Description: " + mainDescription + " not found");
                        return R.drawable.clear;
                }
            }

            public String getHumidity() {
                return mHumidity;
            }

            public void setHumidity(String humidity) {
                mHumidity = humidity;
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
                return formatTime(mTime);
            }

            public void setTime(long time) {
                mTime = time;
            }

            public String getTemp() {
                return mTemp;
            }

            public void setTemp(String temp) {
                this.mTemp = temp;
            }

            public String getTemp_min() {
                return mTemp_min;
            }

            public void setTemp_min(String temp_min) {
                this.mTemp_min = temp_min;
            }

            public String getTemp_max() {
                return mTemp_max;
            }

            public void setTemp_max(String temp_max) {
                this.mTemp_max = temp_max;
            }
        }
    }
}
