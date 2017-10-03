package com.siddapps.android.simpleweather;

import android.util.Log;

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

    public static String formatTemp(String temp) {
        Double tempDouble = Double.parseDouble(temp);

        switch (MainActivity.TEMPERATURE_SETTING) {
            case "C":
                tempDouble = tempDouble - 273.15; //Celsius
                return String.format("%.0f°C", tempDouble);
            case "F":
            default:
                tempDouble = tempDouble * (9 / 5d) - 459.67; //Fahrenheit
                return String.format("%.0f°F", tempDouble);
        }
    }

    public ExtendedForecast getExtendedForecast() {
        Log.i(TAG, "checking for extendedForecast in " + name);
        if (mExtendedForecast == null) {
            Log.i(TAG, "creating new Extended weather");
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
        return formatTemp(temp);
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getTemp_min() {
        return formatTemp(temp_min);
    }

    public void setTemp_min(String temp_min) {
        this.temp_min = temp_min;
    }

    public String getTemp_max() {
        return formatTemp(temp_max);
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

        private ExtendedForecast() {
            Log.i(TAG, "Checking to see if new List needed...");
            if (mHourlyDataList == null) {
                Log.i(TAG, "List created");
                mHourlyDataList = new ArrayList<>();
            } else {
                Log.i(TAG, "List not created");
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
                return formatTemp(mTemp);
            }

            public void setTemp(String temp) {
                this.mTemp = temp;
            }

            public String getTemp_min() {
                return formatTemp(mTemp_min);
            }

            public void setTemp_min(String temp_min) {
                this.mTemp_min = temp_min;
            }

            public String getTemp_max() {
                return formatTemp(mTemp_max);
            }

            public void setTemp_max(String temp_max) {
                this.mTemp_max = temp_max;
            }
        }
    }
}
