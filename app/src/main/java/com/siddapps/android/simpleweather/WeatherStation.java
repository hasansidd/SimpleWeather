package com.siddapps.android.simpleweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
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
    private static final String SHARED_PREF_LIST = "sharedPrefList";
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

    public Weather addWeather(String source) throws Exception {
        Weather weather = mWeatherFetcher.fetchWeather(source);

        if (!mWeathers.isEmpty()) {
            for (int i = 0; i < mWeathers.size(); i++) {
                if (mWeathers.get(i).getName().contains(weather.getName())) {
                    Log.i(TAG, "City: " + weather.getName() + " already exists, moving to beginning of list");
                    mWeathers.remove(i);
                    mWeathers.add(0, weather);
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
            if (mWeathers.get(i).getName().contains(weather.getName())) {
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
        Log.i(TAG, "GetHOURLYDATAAA : " + weather.getName());
        List<Weather.ExtendedForecast.HourlyData> hourlyData = extendedForecast.getHourlyDataList();
        Log.i(TAG, "GetHOURLYDATAAA : " + hourlyData.get(0).getTemp());
        return hourlyData;
    }

    public Weather addCurrentWeather() throws Exception {
        Weather weather = mWeatherFetcher.fetchCurrentWeather();
        if (weather == null) {
            Log.i(TAG, "addCurrentWeather weather is null");
            return null;
        }

        if (!mWeathers.isEmpty()) {
            for (int i = 0; i < mWeathers.size(); i++) {
                if (mWeathers.get(i).getName().contains(weather.getName())) {
                    Log.i(TAG, mWeathers.get(i).getName());
                    mWeathers.remove(i);
                    mWeathers.add(0, weather);
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
    public Weather updateWeathers() throws Exception {
        if (!mWeathers.isEmpty()) {
            for (int i = 0; i < mWeathers.size(); i++) {
                Weather.ExtendedForecast extendedForecast = mWeathers.get(i).getExtendedForecast();
                Weather weather = mWeatherFetcher.fetchWeather(mWeathers.get(i).getName());
                if (!extendedForecast.getHourlyDataList().isEmpty()) {
                    weather.setExtendedForecast(extendedForecast);
                }
                mWeathers.set(i, weather);
            }
            return mWeathers.get(0);
        }
        return null;
    }

    public Observable updateWeathersObservable() {
        if (!mWeathers.isEmpty() && mWeathers != null) {
            Log.i(TAG, String.valueOf(mWeathers));
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
        List<Weather> weathers = getWeathers();
        Set<String> cityNameSet = new HashSet<String>();

        SharedPreferences sharedPreferences = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        for (Weather weather : weathers) {
            cityNameSet.add(weather.getName());
        }
        sharedPreferences.edit().putStringSet(SHARED_PREF_LIST, cityNameSet).apply();
    }

    public List<String> getSharedPreferences() throws Exception {
        if (mWeathers.size() > 0) {
            Log.i(TAG, "mWeathers is not empty: " + mWeathers.size());
            return null;
        }

        SharedPreferences sharedPreferences = mContext.getSharedPreferences(mContext.getPackageName(), Context.MODE_PRIVATE);
        Set<String> cityNameSet = sharedPreferences.getStringSet(SHARED_PREF_LIST, new HashSet<String>());
/*        List<Weather> weathers = new ArrayList<>();

        for (String cityNames : cityNameSet) {
            weathers.add(mWeatherFetcher.fetchWeather(cityNames));
        }
        setWeathers(weathers);*/

        ArrayList<String> cityNameList = new ArrayList<>(cityNameSet);
        return cityNameList;
    }

    public void deleteWeather(Weather weather) {
        mWeathers.remove(weather);
    }


}
