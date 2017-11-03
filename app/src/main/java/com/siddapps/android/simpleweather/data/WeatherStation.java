package com.siddapps.android.simpleweather.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.evernote.android.job.JobManager;
import com.siddapps.android.simpleweather.R;
import com.siddapps.android.simpleweather.weatherjobs.WeatherFetchJob;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class WeatherStation {
    private static final String TAG = "WeatherStation";
    private static final String SHARED_PREF_MAP = "sharedPrefList";
    private static String SHARED_TEMPERATURE_SETTING = "temperatureSetting";
    private List<Weather> mWeathers;
    private static WeatherStation sWeatherStation;
    WeatherFetcher mWeatherFetcher;

    public static WeatherStation get() {
        if (sWeatherStation == null) {
            sWeatherStation = new WeatherStation();
        }
        return sWeatherStation;
    }

    private WeatherStation() {
        mWeatherFetcher = new WeatherFetcher();
        mWeathers = new ArrayList<>();
    }

    public List<Weather> getWeathers() {
        return mWeathers;
    }


    public Weather getWeather(String citySource) {
        for (int i = 0; i < mWeathers.size(); i++) {
            if (mWeathers.get(i).getSource().contains(citySource) || mWeathers.get(i).getName().contains(citySource)) {
                // Log.i(TAG, "Found weather for " + mWeathers.get(i).getName());
                return mWeathers.get(i);
            }
        }
        Log.i(TAG, "Could not find weather");
        return null;
    }

    private int getWeatherIndex(Weather weather) {
        if (!mWeathers.isEmpty()) {
            for (int i = 0; i < mWeathers.size(); i++) {
                if (mWeathers.get(i).getName().equals(weather.getName()) || mWeathers.get(i).getLatLon().equals(weather.getLatLon())) {
                    return i;
                }
            }
        }
        return -1;
    }

    private Weather addWeather(String source) throws Exception {
        Weather weather = mWeatherFetcher.fetchWeather(source);
        int index = getWeatherIndex(weather);

        if (index == -1) {
            mWeathers.add(weather);
            Log.i(TAG, "City: " + weather.getName() + " added to end of list");
            return weather;
        }

        Log.i(TAG, "City: " + weather.getName() + " already exists, moving to beginning of list");
        Weather.ExtendedForecast extendedForecast = weather.getExtendedForecast();
        mWeathers.remove(index);
        mWeathers.add(0, weather);
        mWeathers.get(0).setExtendedForecast(extendedForecast);
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
        int index = getWeatherIndex(weather);
        if (index == -1) {
            Log.i(TAG, "Could not get extended forecast for: " + weather.getName());
            return null;
        }

        Log.i(TAG, "Getting extended forecast for: " + weather.getName());
        mWeathers.set(index, mWeatherFetcher.fetchExtendedForecast(weather));
        return weather;
    }

    private Weather addCurrentWeather(Context context) throws Exception {
        Weather weather = mWeatherFetcher.fetchCurrentWeather(context);
        if (weather == null) {
            Log.e(TAG, "addCurrentWeather weather is null");
            return null;
        }

        int index = getWeatherIndex(weather);
        if (index == -1) {
            Log.i(TAG, "City: " + weather.getName() + " added to beginning of list");
            mWeathers.add(0, weather);
            return weather;
        }

        Weather.ExtendedForecast extendedForecast = mWeathers.get(index).getExtendedForecast();
        mWeathers.remove(index);
        mWeathers.add(0, weather);
        mWeathers.get(0).setExtendedForecast(extendedForecast);
        Log.i(TAG, "City: " + weather.getName() + " already exists, moved to beginning of list");
        return weather;
    }

    public Observable addCurrentWeatherObservable(final Context context) {
        return Observable.defer(new Callable<ObservableSource<Weather>>() {
            @Override
            public ObservableSource<Weather> call() throws Exception {
                return Observable.just(addCurrentWeather(context));
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
            long minUpdateTime = TimeUnit.MINUTES.toMillis(30);
            if (mWeathers.get(0).getTimeFetched() < (Calendar.getInstance().getTimeInMillis() - minUpdateTime)) {
                for (int i = 0; i < mWeathers.size(); i++) {
                    Weather.ExtendedForecast extendedForecast = mWeathers.get(i).getExtendedForecast();
                    Weather weather = mWeatherFetcher.fetchWeather(mWeathers.get(i).getSource());
                    weather.setExtendedForecast(extendedForecast);
                    mWeathers.set(i, weather);
                }
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

    public void shouldCancelJobs() {
        boolean shouldCancelJobs = true;

        for (Weather w : mWeathers) {
            if (w.getExtendedForecast().isNotifyReady()) {
                shouldCancelJobs = false;
            }
        }

        if (shouldCancelJobs) {
            Log.i(TAG, "ALL JOBS CANCELED");
            JobManager.instance().cancelAllForTag(WeatherFetchJob.TAG);
        }
    }

    public void setSharedPreferences(Context context) {
        LinkedHashMap<String, Boolean> weathersMap = new LinkedHashMap<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);

        for (Weather w : mWeathers) {
            Weather.ExtendedForecast extendedForecast = w.getExtendedForecast();
            weathersMap.put(w.getSource(), extendedForecast.isNotifyReady());
        }

        JSONObject jsonObject = new JSONObject(weathersMap);
        String jsonString = jsonObject.toString();

        sharedPreferences.edit().putString(SHARED_PREF_MAP, jsonString).apply();
    }

    public List<Weather> getSharedPreferences(Context context) throws Exception {
        if (mWeathers.size() > 0) {
            return mWeathers;
        }

        LinkedHashMap<String, Boolean> weathersMap = new LinkedHashMap<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);

        if (sharedPreferences.contains(SHARED_PREF_MAP)) {
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
                weather.getExtendedForecast().setNotifyReady(weathersMap.get(key));
            }

            return mWeathers;
        }
        return null;
    }

    public Observable getSharedPreferencesObservable(final Context context) {
        if (mWeathers.size() > 0) {
            return null;
        }

        return Observable.defer(new Callable<ObservableSource<List<Weather>>>() {
            @Override
            public ObservableSource<List<Weather>> call() throws Exception {
                return Observable.just(getSharedPreferences(context));
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void deleteWeather(Weather weather) {
        mWeathers.remove(weather);
    }

    public String getTempSetting(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(context.getString(R.string.pref_temp_units_key), context.getString(R.string.pref_temp_units_default));
    }

    public String formatTemp(Context context, String temp) {
        Double tempDouble = Double.parseDouble(temp);
        String tempSetting = getTempSetting(context);

        switch (tempSetting) {
            case "C":
                tempDouble = tempDouble - 273.15; //Celsius
                return String.format("%.0f°C", tempDouble);
            case "F":
            default:
                tempDouble = tempDouble * (9 / 5d) - 459.67; //Fahrenheit
                return String.format("%.0f°F", tempDouble);
        }
    }
}
