package com.siddapps.android.simpleweather.data;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.siddapps.android.simpleweather.util.LocationUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherFetcher {
    private static final String TAG = "WeatherFetcher";
    private static final String API_KEY = "7c3f5df254d3c24ed9ed5f0ab0937b49";
    private static final String METHOD_EXTENDED = "forecast";
    private static final String METHOD_CURRENT = "weather";
    private LocationUtil mLocationUtil;

    public WeatherFetcher(Context context) {
        if (mLocationUtil == null) {
            mLocationUtil = new LocationUtil(context);
        }
    }

    private String fetchJson(String source, String methodType) throws Exception {
        String result = "";

        String urlString = source.replaceAll("\\s", "");
        URL url = new URL("http://api.openweathermap.org/data/2.5/" + methodType + "?q=" + urlString + "&APPID=" + API_KEY);

        if (source.matches(".*\\d+.*")) {
            url = new URL("http://api.openweathermap.org/data/2.5/" + methodType + "?zip=" + source + ",us&APPID=" + API_KEY);
        }

        if (source.contains(",")) {
            String[] location = source.split(",");
            url = new URL("http://api.openweathermap.org/data/2.5/" + methodType + "?lat=" + location[0] + "&lon=" + location[1] + "&APPID=" + API_KEY);
        }


        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        InputStream in = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(in);
        int temp = reader.read();

        while (temp != -1) {
            result += (char) temp;
            temp = reader.read();
        }
        return result;
    }

    public Weather fetchWeather(String source) throws Exception {
        if (source == null) {
            Log.i(TAG, "getWeather source is null");
            return null;
        }

        //Log.i(TAG, "Fetching weather for city: " + source);

        Weather mWeather = new Weather();
        String json = fetchJson(source, METHOD_CURRENT);

        JSONObject jsonObject = new JSONObject(json);
        mWeather.setName(jsonObject.getString("name"));
        mWeather.setTime(jsonObject.getLong("dt"));

        JSONObject coordInfoObject = jsonObject.getJSONObject("coord");
        mWeather.setLat(coordInfoObject.getString("lat"));
        mWeather.setLon(coordInfoObject.getString("lon"));

        JSONArray weatherInfoArray = new JSONArray(jsonObject.getString("weather"));
        JSONObject weatherInfoObject = weatherInfoArray.getJSONObject(0);
        mWeather.setMainDescription(weatherInfoObject.getString("main"));
        mWeather.setDetailedDescription(weatherInfoObject.getString("description"));

        JSONObject mainInfoObject = jsonObject.getJSONObject("main");
        mWeather.setTemp(mainInfoObject.getString("temp"));
        mWeather.setHumidity(mainInfoObject.getString("humidity"));
        mWeather.setTemp_min(mainInfoObject.getString("temp_min"));
        mWeather.setTemp_max(mainInfoObject.getString("temp_max"));

        JSONObject sysInfoObject = jsonObject.getJSONObject("sys");
        mWeather.setSunrise(sysInfoObject.getLong("sunrise"));
        mWeather.setSunset(sysInfoObject.getLong("sunset"));

        // printCurrentWeather(mWeather);
        return mWeather;
    }

    public Weather fetchExtendedForecast(Weather weather) throws Exception {
        Weather.ExtendedForecast extendedForecast = weather.getExtendedForecast();
        String json = fetchJson(weather.getName(), METHOD_EXTENDED);

        JSONObject jsonObject = new JSONObject(json);
        JSONArray fullInfoArray = new JSONArray(jsonObject.getString("list"));

        for (int i = 0; i < fullInfoArray.length(); i++) {
            Weather.ExtendedForecast.HourlyData hourlyData = new Weather.ExtendedForecast.HourlyData();
            JSONObject hourlyInfoObject = fullInfoArray.getJSONObject(i);
            hourlyData.setTime(hourlyInfoObject.getLong("dt"));

            JSONObject mainInfoObject = hourlyInfoObject.getJSONObject("main");
            hourlyData.setTemp(mainInfoObject.getString("temp"));
            hourlyData.setTemp_min(mainInfoObject.getString("temp_min"));
            hourlyData.setTemp_max(mainInfoObject.getString("temp_max"));
            hourlyData.setHumidity(mainInfoObject.getString("humidity"));

            JSONArray weatherInfoArray = hourlyInfoObject.getJSONArray("weather");
            JSONObject weatherInfoObject = weatherInfoArray.getJSONObject(0);
            hourlyData.setMainDescription(weatherInfoObject.getString("main"));
            hourlyData.setDetailedDescription(weatherInfoObject.getString("description"));
            hourlyData.setNight(weatherInfoObject.getString("icon"));

            //printExtendedForecastWeather(hourlyData);
            extendedForecast.addHourlyData(hourlyData);
        }

        weather.setExtendedForecast(extendedForecast);
        return weather;
    }

    public Weather fetchCurrentWeather() throws Exception {
        Location location = mLocationUtil.getCurrentLocationLatLon();
        String source = String.format("%f,%f", location.getLatitude(), location.getLongitude());

        //String source = mLocationUtil.getCurrentLocationZip();
        return fetchWeather(source);
    }

    private void printCurrentWeather(Weather weather) {
        Weather mWeather = weather;
        String fullInfo = String.format("Name: %s\nMain: %s\nDescription: %s\nTemperature: %s\nHumidity: %s\nMin Temp: %s\nMax Temp: %s\n"
                , mWeather.getName()
                , mWeather.getMainDescription()
                , mWeather.getDetailedDescription()
                , mWeather.getTemp()
                , mWeather.getHumidity()
                , mWeather.getTemp_min()
                , mWeather.getTemp_max());

        Log.i(TAG, fullInfo);
    }

    public void printExtendedForecastWeather(Weather.ExtendedForecast.HourlyData hourlyData) {
        String fullInfo = String.format("Main: %s\nDescription: %s\nTemperature: %s\nHumidity: %s\nMin Temp: %s\nMax Temp: %s\nTime: %s\n"
                , hourlyData.getMainDescription()
                , hourlyData.getDetailedDescription()
                , hourlyData.getTemp()
                , hourlyData.getHumidity()
                , hourlyData.getTemp_min()
                , hourlyData.getTemp_max()
                , hourlyData.getTime());

        Log.i(TAG, fullInfo);
    }


}
