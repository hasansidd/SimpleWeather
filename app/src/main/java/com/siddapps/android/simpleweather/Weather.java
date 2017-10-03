package com.siddapps.android.simpleweather;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
    private int icon;
    private long time;
    private long sunrise;
    private long sunset;
    private ExtendedForecast mExtendedForecast;
    private boolean isExtendedForecastReady;

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
        return formatTime(sunset);    }

    public void setSunset(long sunset) {
        this.sunset = sunset;
    }

    public int getIcon() {
        if (time > sunset || time < sunrise) {//night
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

    public static class ExtendedForecast {
        static List<HourlyData> mHourlyDataList;

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

            public int getIcon() {
                switch (mainDescription) {
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
                Double tempDouble = Double.parseDouble(temp);
                tempDouble = tempDouble * (9 / 5d) - 459.67; //Fahrenheit
                this.mTemp = String.format("%.0f°F", tempDouble);
            }

            public String getTemp_min() {
                return mTemp_min;
            }

            public void setTemp_min(String temp_min) {
                Double temp_minDouble = Double.parseDouble(temp_min);
                temp_minDouble = temp_minDouble * (9 / 5d) - 459.67; //Fahrenheit
                this.mTemp_min = String.format("%.0f°F", temp_minDouble);
            }

            public String getTemp_max() {
                return mTemp_max;
            }

            public void setTemp_max(String temp_max) {
                Double temp_maxDouble = Double.parseDouble(temp_max);
                temp_maxDouble = temp_maxDouble * (9 / 5d) - 459.67; //Fahrenheit
                this.mTemp_max = String.format("%.0f°F", temp_maxDouble);
            }
        }
    }
}
