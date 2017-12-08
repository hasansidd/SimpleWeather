package com.siddapps.android.simpleweather.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.evernote.android.job.JobManager;
import com.siddapps.android.simpleweather.R;
import com.siddapps.android.simpleweather.data.model.HourlyData;
import com.siddapps.android.simpleweather.data.model.Weather;
import com.siddapps.android.simpleweather.weatherjobs.WeatherFetchJob;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.siddapps.android.simpleweather.util.TimeUtil.formatTime;

public class WeatherStation {
    private static final String TAG = "WeatherStation";
    private static final String SHARED_PREF_MAP = "sharedPrefList";
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

    public List<Weather> getWeathers(Context context) {
        WeatherDatabase db = WeatherDatabase.getInstance(context);
        List<Weather> weathers = db.weatherDao().getWeathers();
        return weathers;
    }

    public List<Weather> getNotifyReadyWeathers(Context context) {
        WeatherDatabase db = WeatherDatabase.getInstance(context);
        List<Weather> weathers = db.weatherDao().getNotifyReadyWeathers();
        return weathers;
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

    public List<HourlyData> getExtendedWeather(Context context, Weather weather) throws Exception {
        List<HourlyData> hourlyDataList = mWeatherFetcher.fetchExtendedForecast(context, weather);
        return hourlyDataList;
    }

    public List<HourlyData> getExtendedWeathers(Context context, List<Weather> weathers) throws Exception {
        List<HourlyData> hourlyDataList = mWeatherFetcher.fetchExtendedForecasts(context, weathers);
        return hourlyDataList;
    }

    private Weather addCurrentWeather(Context context) throws Exception {
        WeatherDatabase db = WeatherDatabase.getInstance(context);
        prepareCurrent(context);

        Weather weather = mWeatherFetcher.fetchCurrentWeather(context);

        if (weather == null) {
            Log.i(TAG, "addCurrentWeather weather is null");
            return null;
        }

        boolean isNotifyReady = db.weatherDao().isNotifyReadyFromName(weather.getName());

        weather.setCurrent(true);
        weather.setNotifyReady(isNotifyReady);
        Log.e(TAG, weather.getName() + " set as " + weather.isCurrent());

        db.weatherDao().addWeather(weather);
        Log.e(TAG, "Done adding");
        return weather;
    }

    private void prepareCurrent(Context context) {
        WeatherDatabase db = WeatherDatabase.getInstance(context);
        List<Weather> weathers = db.weatherDao().getWeathers();
        for (Weather w : weathers) {
            w.setCurrent(false);
        }

        db.weatherDao().updateWeathers(weathers);
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

    private void updateWeather(Context context, Weather weather) {
        WeatherDatabase db = WeatherDatabase.getInstance(context);
        db.weatherDao().updateWeather(weather);
    }

    private Weather updateWeathers(Context context) throws Exception {
        WeatherDatabase db = WeatherDatabase.getInstance(context);
        List<Weather> weathers = db.weatherDao().getWeathers();

        List<Weather> newWeathers = new ArrayList<>();
        for (int i = 0; i<weathers.size(); i++) {
            Weather weather = mWeatherFetcher.fetchWeather(weathers.get(i).getSource());
            weather.setCurrent(weathers.get(i).isCurrent());
            newWeathers.add(weather);
            Log.e("new", newWeathers.get(i).getName() + " : " + newWeathers.get(i).isCurrent());
            Log.e("old", weathers.get(i).getName() + " : " + weathers.get(i).isCurrent());
        }

        db.weatherDao().updateWeathers(newWeathers);
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

    public void toggleRainNotification(Context context, Weather weather) {
        if (weather.isNotifyReady()) {
            weather.setNotifyReady(false);
            shouldCancelJobs();
        } else {
            weather.setNotifyReady(true);
            WeatherFetchJob.scheduleJobOnce();
        }

        updateWeather(context, weather);
    }

    private void shouldCancelJobs() {
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
