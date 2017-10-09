package com.siddapps.android.simpleweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class WeatherStation {
    private static final String TAG = "WeatherStation";
    private static final String SHARED_PREF_MAP = "sharedPrefList";
    private static String SHARED_TEMPERATURE_SETTING = "temperatureSetting";
    private List<Weather> mWeathers;
    private static WeatherStation sWeatherStation;
    WeatherFetcher mWeatherFetcher;
    private Context mContext;


    public static WeatherStation get(Context context) {
        if (sWeatherStation == null) {
            sWeatherStation = new WeatherStation(context);
        }
        return sWeatherStation;
    }

    private WeatherStation(Context context) {
        mContext = context.getApplicationContext();
        mWeatherFetcher = new WeatherFetcher(mContext);
        mWeathers = new ArrayList<>();
    }

    public List<Weather> getWeathers() {
        return mWeathers;
    }

    public void setWeathers(List<Weather> weathers) {
        mWeathers = weathers;
        return;
    }

    public Weather getWeather(String cityName) {
        for (int i = 0; i < mWeathers.size(); i++) {
            if (mWeathers.get(i).getName().contains(cityName)) {
                // Log.i(TAG, "Found weather for " + mWeathers.get(i).getName());
                return mWeathers.get(i);
            }
        }
        Log.i(TAG, "Could not find weather");
        return null;
    }

    public void setWeather(Weather weather) {
        for (int i = 0; i < mWeathers.size(); i++) {
            if (mWeathers.get(i).getName().contains(weather.getName())) {
                mWeathers.set(i, weather);
            }
        }
    }

    private Weather addWeather(String source) throws Exception {
        Weather weather = mWeatherFetcher.fetchWeather(source);

        if (!mWeathers.isEmpty()) {
            for (int i = 0; i < mWeathers.size(); i++) {
                if (mWeathers.get(i).getName().contains(weather.getName())) {
                    Log.i(TAG, "City: " + weather.getName() + " already exists, moving to beginning of list");
                    Weather.ExtendedForecast extendedForecast = weather.getExtendedForecast();
                    mWeathers.remove(i);
                    mWeathers.add(0, weather);
                    mWeathers.get(0).setExtendedForecast(extendedForecast);
                    return weather;
                }
            }
        }
        mWeathers.add(weather);
        Log.i(TAG, "City: " + weather.getName() + " added to end of list");
        return weather;
    }

    public Observable addWeatherObservable(final String source) {
        return Observable.defer(new Callable<ObservableSource<Weather>>() {
            @Override
            public ObservableSource<Weather> call() throws Exception {
                return Observable.just(addWeather(source));
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Weather getExtendedWeather(Weather weather) throws Exception {
        for (int i = 0; i < mWeathers.size(); i++) {
            if (mWeathers.get(i).getName().equals(weather.getName())) {
                Log.i(TAG, "Getting extended forecast for: " + weather.getName());
                mWeathers.set(i, mWeatherFetcher.fetchExtendedForecast(weather));
                return weather;
            }
        }
        Log.i(TAG, "Could not get extended forecast for: " + weather.getName());
        return null;
    }

    public List<Weather.ExtendedForecast.HourlyData> getHourlyData(Weather weather) {
        Weather.ExtendedForecast extendedForecast = weather.getExtendedForecast();
        List<Weather.ExtendedForecast.HourlyData> hourlyData = extendedForecast.getHourlyDataList();
        return hourlyData;
    }

    private Weather addCurrentWeather() throws Exception {
        Weather weather = mWeatherFetcher.fetchCurrentWeather();
        if (weather == null) {
            Log.i(TAG, "addCurrentWeather weather is null");
            return null;
        }

        if (!mWeathers.isEmpty()) {
            for (int i = 0; i < mWeathers.size(); i++) {
                if (mWeathers.get(i).getName().equals(weather.getName())) {
                    Weather.ExtendedForecast extendedForecast = mWeathers.get(i).getExtendedForecast();
                    mWeathers.remove(i);
                    mWeathers.add(0, weather);
                    mWeathers.get(0).setExtendedForecast(extendedForecast);
                    Log.i(TAG, "City: " + weather.getName() + " already exists, moving to beginning of list");
                    return weather;
                }
            }
        }
        Log.i(TAG, "City: " + weather.getName() + " added to beginning of list");
        mWeathers.add(0, weather);
        return weather;
    }

    public Observable addCurrentWeatherObservable() {
        return Observable.defer(new Callable<ObservableSource<Weather>>() {
            @Override
            public ObservableSource<Weather> call() throws Exception {
                return Observable.just(addCurrentWeather());
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }


    //This method needs to be reworked. When going back from detailed view, this method is run.
    //When it is ran, the weather object containing the ExtendedForecast is overwritten
    //with the new weather object. This erases the Extended Forecast. Currently the work around is to
    //extract the Extended forecast before overwriting the weather object and then re-adding it, only
    //if it contained data before. Yeah I know great programming.
    // TODO: Rework to use setters to update weather instead of replacing object.
    private Weather updateWeathers() throws Exception {

        if (!mWeathers.isEmpty()) {
            for (int i = 0; i < mWeathers.size(); i++) {
                Weather.ExtendedForecast extendedForecast = mWeathers.get(i).getExtendedForecast();
                Weather weather = mWeatherFetcher.fetchWeather(mWeathers.get(i).getName());

                weather.setExtendedForecast(extendedForecast);

                mWeathers.set(i, weather);
            }
            return mWeathers.get(0);
        }
        return null;
    }

    public Observable updateWeathersObservable() {
        if (!mWeathers.isEmpty() && mWeathers != null) {
            return Observable.defer(new Callable<ObservableSource<Weather>>() {
                @Override
                public ObservableSource<Weather> call() throws Exception {
                    return Observable.just(updateWeathers());
                }
            })
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread());
        }
        return null;
    }

    public void setSharedPreferences() {
        HashMap<String, Boolean> weathersMap = new HashMap<>();
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);

        for (Weather w : mWeathers) {
            Weather.ExtendedForecast extendedForecast = w.getExtendedForecast();
            weathersMap.put(w.getName(), extendedForecast.isNotifyReady());
        }

        JSONObject jsonObject = new JSONObject(weathersMap);
        String jsonString = jsonObject.toString();

        sharedPreferences.edit().putString(SHARED_PREF_MAP, jsonString).apply();
    }

    public List<Weather> getSharedPreferences() throws Exception {

        HashMap<String, Boolean> weathersMap = new HashMap<>();
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);

        if (sharedPreferences != null) {
            String jsonString = sharedPreferences.getString(SHARED_PREF_MAP, (new JSONObject()).toString());
            JSONObject jsonObject = new JSONObject(jsonString);

            Iterator<String> keysItr = jsonObject.keys();
            while (keysItr.hasNext()) {
                String key = keysItr.next();
                Boolean value = (Boolean) jsonObject.get(key);
                weathersMap.put(key, value);
            }

            for (String key : weathersMap.keySet()) {
                Weather weather = addWeather(key);
                Weather.ExtendedForecast extendedForecast = weather.getExtendedForecast();
                extendedForecast.setNotifyReady(weathersMap.get(key));
            }

            for (Weather w : mWeathers) {
            }

            return mWeathers;
        }

        return null;
    }

    public Observable getSharedPreferencesObservable() {
        if (mWeathers.size() > 0) {
            return null;
        }

        return Observable.defer(new Callable<ObservableSource<List<Weather>>>() {
            @Override
            public ObservableSource<List<Weather>> call() throws Exception {
                return Observable.just(getSharedPreferences());
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void deleteWeather(Weather weather) {
        mWeathers.remove(weather);
    }

    public String getTempSetting() {
        String tempSetting = "";
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        if (sharedPreferences.getString(SHARED_TEMPERATURE_SETTING, "F") != null) {
            tempSetting = sharedPreferences.getString(SHARED_TEMPERATURE_SETTING, "F");
        }
        return tempSetting;
    }

    public void setTempSetting(String string) {
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(SHARED_TEMPERATURE_SETTING, string).apply();
    }
}
