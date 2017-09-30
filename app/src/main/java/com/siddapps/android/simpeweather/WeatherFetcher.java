package com.siddapps.android.simpeweather;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherFetcher {
    private static final String TAG = "WeatherFetcher";
    private static final String API_KEY = "7c3f5df254d3c24ed9ed5f0ab0937b49";
    private LocationUtil mLocationUtil;

    public WeatherFetcher(Context context) {
        if (mLocationUtil == null) {
            mLocationUtil = new LocationUtil(context);
        }
    }

    private String getJson(String source) throws Exception {
        String result = "";
        String urlString = source.replaceAll("\\s", "");
        URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + urlString + "&APPID=" + API_KEY);

        if (source.matches(".*\\d+.*")) {
            url = new URL("http://api.openweathermap.org/data/2.5/weather?zip=" + source + ",us&APPID=" + API_KEY);
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

    public Weather getWeather(String source) throws Exception {
        if (source == null) {
            Log.i(TAG, "getWeather source is null");
            return null;
        }

        //Log.i(TAG, "Fetching weather for city: " + source);

        Weather mWeather = new Weather();
        String json = getJson(source);

        JSONObject jsonObject = new JSONObject(json);
        mWeather.setName(jsonObject.getString("name"));
        mWeather.setTime(jsonObject.getLong("dt"));

        JSONArray weatherInfoArray = new JSONArray(jsonObject.getString("weather"));
        JSONObject weatherInfoObject = weatherInfoArray.getJSONObject(0);
        mWeather.setMainDescription(weatherInfoObject.getString("main"));
        mWeather.setDetailedDescription(weatherInfoObject.getString("description"));
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

        printCurrentWeather(mWeather);

        return mWeather;
    }

    public Weather getCurrentWeather() throws Exception {
        String source = mLocationUtil.getCurrentLocationZip();
        return getWeather(source);
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


}
