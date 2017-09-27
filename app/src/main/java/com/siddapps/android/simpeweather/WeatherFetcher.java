package com.siddapps.android.simpeweather;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherFetcher {
    private static final String TAG = "WeatherFetcher";
    private static final String API_KEY = "7c3f5df254d3c24ed9ed5f0ab0937b49";
    private Weather mWeather;

    public WeatherFetcher() {
        mWeather = new Weather();
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

    public void getCurrentWeather(String source) throws Exception {
        String json = getJson(source);

        Log.i(TAG, json);

        JSONObject jsonObject = new JSONObject(json);
        String weatherInfo = jsonObject.getString("weather");
        JSONArray weatherInfoArray = new JSONArray(weatherInfo);
        JSONObject jsonPart = weatherInfoArray.getJSONObject(0);
        JSONObject mainInfoObject = jsonObject.getJSONObject("main");

        mWeather.setName(jsonObject.getString("name"));
        mWeather.setMain(jsonPart.getString("main"));
        mWeather.setDescription(jsonPart.getString("description"));
        mWeather.setTemp(mainInfoObject.getString("temp"));
        mWeather.setHumidity(mainInfoObject.getString("humidity"));
        mWeather.setTemp_min(mainInfoObject.getString("temp_min"));
        mWeather.setTemp_max(mainInfoObject.getString("temp_max"));
        printCurrentWeather();
    }

    private void printCurrentWeather() {
        String fullInfo = String.format("Name: %s\n Main: %s\n Description: %s\n Temperature: %s\n Humidity: %s\n Min Temp: %s\n Max Temp: %s\n"
                , mWeather.getName()
                , mWeather.getMain()
                , mWeather.getDescription()
                , mWeather.getTemp()
                , mWeather.getHumidity()
                , mWeather.getTemp_min()
                , mWeather.getTemp_max());

        Log.i(TAG, fullInfo);
    }


}
