package com.siddapps.android.simpleweather.data;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.siddapps.android.simpleweather.data.model.HourlyData;
import com.siddapps.android.simpleweather.data.model.Weather;
import com.siddapps.android.simpleweather.util.LocationUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WeatherFetcher {
    private final String TAG = this.getClass().getSimpleName();
    private static final String API_KEY = "7c3f5df254d3c24ed9ed5f0ab0937b49";
    public static final String SOURCE_CITY = "city";
    public static final String SOURCE_ZIP = "zip";
    public static final String SOURCE_LATLON = "latlon";
    private static final String METHOD_EXTENDED = "forecast";
    private static final String METHOD_CURRENT = "weather";

    private String[] fetchWeatherByType(String source, String methodType) throws Exception {
        String urlString = source.replaceAll("\\s", "+");
        URL url = new URL("http://api.openweathermap.org/data/2.5/" + methodType + "?q=" + urlString + "&APPID=" + API_KEY); //by city name
        String sourceType = SOURCE_CITY;

        if (source.matches(".*\\d+.*")) {
            url = new URL("http://api.openweathermap.org/data/2.5/" + methodType + "?zip=" + source + ",us&APPID=" + API_KEY); //by zip
            sourceType = SOURCE_ZIP;
        }

        if (source.contains(",")) {
            String[] location = source.split(",");
            url = new URL("http://api.openweathermap.org/data/2.5/" + methodType + "?lat=" + location[0] + "&lon=" + location[1] + "&APPID=" + API_KEY); //by lon/lat
            sourceType = SOURCE_LATLON;
        }

        String json = fetchJson(url);
        return new String[] {json, sourceType};
    }

    private String fetchJson(URL url) throws Exception {
        String result = "";

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

        Weather weather = new Weather();
        String weatherInfo[] = fetchWeatherByType(source, METHOD_CURRENT);
        String json = weatherInfo[0];

        JSONObject jsonObject = new JSONObject(json);
        weather.setName(jsonObject.getString("name"));
        weather.setTime(jsonObject.getLong("dt"));

        JSONObject coordInfoObject = jsonObject.getJSONObject("coord");
        weather.setLat(coordInfoObject.getString("lat"));
        weather.setLon(coordInfoObject.getString("lon"));

        JSONArray weatherInfoArray = new JSONArray(jsonObject.getString("weather"));
        JSONObject weatherInfoObject = weatherInfoArray.getJSONObject(0);
        weather.setMainDescription(weatherInfoObject.getString("main"));
        weather.setDetailedDescription(weatherInfoObject.getString("description"));

        JSONObject mainInfoObject = jsonObject.getJSONObject("main");
        weather.setTemp(mainInfoObject.getString("temp"));
        weather.setTemp_min(mainInfoObject.getString("temp_min"));
        weather.setTemp_max(mainInfoObject.getString("temp_max"));

        JSONObject sysInfoObject = jsonObject.getJSONObject("sys");
        weather.setSunrise(sysInfoObject.getLong("sunrise"));
        weather.setSunset(sysInfoObject.getLong("sunset"));

        weather.setSourceType(weatherInfo[1]);

        if (weatherInfo[1].equals(SOURCE_ZIP)) {
            weather.setZipCode(source);
        }

        weather.setTimeFetched(Calendar.getInstance().getTimeInMillis());

        // printCurrentWeather(weather);
        return weather;
    }

    private ArrayList<HourlyData> fetchHourlyData(Weather weather) throws Exception {
        Log.e(TAG, weather.getName());
        String json = fetchWeatherByType(weather.getSource(), METHOD_EXTENDED)[0];

        JSONObject jsonObject = new JSONObject(json);
        JSONArray fullInfoArray = new JSONArray(jsonObject.getString("list"));
        JSONObject nameObject = new JSONObject(jsonObject.getString("city"));
        String name = nameObject.getString("name");

        ArrayList<HourlyData> hourlyDataList = new ArrayList<>();

        for (int i = 0; i < fullInfoArray.length(); i++) {
            HourlyData hourlyData = new HourlyData();
            hourlyData.setName(name);

            JSONObject hourlyInfoObject = fullInfoArray.getJSONObject(i);
            hourlyData.setTime(hourlyInfoObject.getLong("dt"));

            JSONObject mainInfoObject = hourlyInfoObject.getJSONObject("main");
            hourlyData.setTemp(mainInfoObject.getString("temp"));
            hourlyData.setTemp_min(mainInfoObject.getString("temp_min"));
            hourlyData.setTemp_max(mainInfoObject.getString("temp_max"));

            JSONArray weatherInfoArray = hourlyInfoObject.getJSONArray("weather");
            JSONObject weatherInfoObject = weatherInfoArray.getJSONObject(0);
            hourlyData.setMainDescription(weatherInfoObject.getString("main"));
            hourlyData.setDetailedDescription(weatherInfoObject.getString("description"));
            hourlyData.setNight(weatherInfoObject.getString("icon"));

            hourlyDataList.add(hourlyData);
            //printExtendedForecastWeather(hourlyData);
            //extendedForecast.addHourlyData(hourlyData);

        }
        return hourlyDataList;
    }

    public ArrayList<HourlyData> fetchExtendedForecasts(Context context, List<Weather> weathers) throws Exception {
        ArrayList<HourlyData> totalHourlyDataList = new ArrayList<>();

        for (Weather w : weathers) {
            List<HourlyData> hourlyDataList = fetchHourlyData(w);
            totalHourlyDataList.addAll(hourlyDataList);
        }

        WeatherDatabase db = WeatherDatabase.getInstance(context);
        db.weatherDao().addHourlyData(totalHourlyDataList);

        for (HourlyData data : totalHourlyDataList) {
            Log.e(TAG, data.getName());
        }

        return totalHourlyDataList;
    }

    public ArrayList<HourlyData> fetchExtendedForecast(Context context, Weather weather) throws Exception {
        WeatherDatabase db = WeatherDatabase.getInstance(context);
        ArrayList<HourlyData> hourlyDataList = fetchHourlyData(weather);
        db.weatherDao().addHourlyData(hourlyDataList);
        return hourlyDataList;
    }

    public Weather fetchCurrentWeather(Context context) throws Exception {
        LocationUtil locationUtil = new LocationUtil(context);
        Location location = locationUtil.getCurrentLocationLatLon();
        String source = String.format("%f,%f", location.getLatitude(), location.getLongitude());

        //String source = mLocationUtil.getCurrentLocationZip();
        return fetchWeather(source);
    }

    private void printCurrentWeather(Weather weather) {
        Weather mWeather = weather;
        String fullInfo = String.format("Name: %s\nMain: %s\nDescription: %s\nTemperature: %s\nMin Temp: %s\nMax Temp: %s\n"
                , mWeather.getName()
                , mWeather.getMainDescription()
                , mWeather.getDetailedDescription()
                , mWeather.getTemp()
                , mWeather.getTemp_min()
                , mWeather.getTemp_max());

        Log.i(TAG, fullInfo);
    }

    public void printExtendedForecastWeather(HourlyData hourlyData) {
        String fullInfo = String.format("Main: %s\nDescription: %s\nTemperature: %s\nMin Temp: %s\nMax Temp: %s\nTime: %s\n"
                , hourlyData.getMainDescription()
                , hourlyData.getDetailedDescription()
                , hourlyData.getTemp()
                , hourlyData.getTemp_min()
                , hourlyData.getTemp_max()
                , hourlyData.getTime());

        Log.i(TAG, fullInfo);
    }


}
