package com.siddapps.android.simpleweather.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.evernote.android.job.JobManager;
import com.siddapps.android.simpleweather.R;
import com.siddapps.android.simpleweather.data.model.ExtendedForecast;
import com.siddapps.android.simpleweather.data.model.Weather;
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

/*    public Weather getWeather(Context context, String citySource) {
        WeatherDatabase db = WeatherDatabase.getInstance(context);
        return db.weatherDao().getWeather(citySource);
    }*/

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

    private Weather addWeather(String source, Context context) throws Exception {
        WeatherDatabase db = WeatherDatabase.getInstance(context);
        Weather weather = mWeatherFetcher.fetchWeather(source);
        db.weatherDao().addWeather(weather);
        return weather;
    }

    public Observable addWeatherObservable(final String source, final Context context) {
        return Observable.defer(new Callable<ObservableSource<Weather>>() {
            @Override
            public ObservableSource<Weather> call() throws Exception {
                return Observable.just(addWeather(source, context));
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
        WeatherDatabase db = WeatherDatabase.getInstance(context);
        Weather weather = mWeatherFetcher.fetchCurrentWeather(context);

        if (weather == null) {
            Log.e(TAG, "addCurrentWeather weather is null");
            return null;
        }

        Log.i(TAG, "City: " + weather.getName() + " added to beginning of list");
        db.weatherDao().addWeather(weather);
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
    private Weather updateWeathers(Context context) throws Exception {
        WeatherDatabase db = WeatherDatabase.getInstance(context);
        List<Weather> weathers = db.weatherDao().getWeathers();
        db.weatherDao().updateWeathers(weathers);
        return weathers.get(0);
    }

    public Observable updateWeathersObservable(final Context context) {
        return Observable.defer(new Callable<ObservableSource<Weather>>() {
            @Override
            public ObservableSource<Weather> call() throws Exception {
                return Observable.just(updateWeathers(context));
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void shouldCancelJobs() {
        boolean shouldCancelJobs = true;

        for (Weather w : mWeathers) {
            if (w.isNotifyReady()) {
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
            weathersMap.put(w.getSource(), w.isNotifyReady());
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
                Weather weather = addWeather(key, context);
                weather.setNotifyReady(weathersMap.get(key));
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

    public void deleteWeather(Weather weather, Context context) {
        WeatherDatabase db = WeatherDatabase.getInstance(context);
        db.weatherDao().deleteWeather(weather);
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
