package com.siddapps.android.simpleweather.WeatherJobs;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.siddapps.android.simpleweather.MainActivity;
import com.siddapps.android.simpleweather.R;
import com.siddapps.android.simpleweather.Weather;
import com.siddapps.android.simpleweather.Weather.ExtendedForecast.HourlyData;
import com.siddapps.android.simpleweather.WeatherStation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WeatherFetchJob extends Job {

    public static final String TAG = "WeatherFetchJob";
    public List<Weather> mWeathers;

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        HashMap<String, String> rainMap = new HashMap<>();


        PendingIntent pi = PendingIntent.getActivity(getContext(), 0,
                new Intent(getContext(), MainActivity.class), 0);

        try {
            rainMap = findRainMap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (rainMap != null && rainMap.size() > 0) {
            String formattedText = "";
            String formattedTitle;

            for (String key : rainMap.keySet()) {
                formattedText += (getContext().getString(R.string.weather_alert_content, key, rainMap.get(key)));
            }

            if (rainMap.size() == 1) {
                formattedTitle = getContext().getString(R.string.weather_alert_title_single);
            } else {
                formattedTitle = (getContext().getString(R.string.weather_alert_title,rainMap.size()));
            }

            Notification notification = new NotificationCompat.Builder(getContext(), "main")
                    .setContentTitle(formattedTitle)
                    .setAutoCancel(true)
                    .setContentIntent(pi)
                    .setSmallIcon(R.drawable.notification)
                    .setLargeIcon(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.clear))
                    .setShowWhen(true)
                    .setColor(getContext().getResources().getColor(R.color.colorAccent))
                    .setLocalOnly(true)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(formattedText))
                    .build();

            NotificationManagerCompat.from(getContext())
                    .notify(1, notification);

            Log.e(TAG, "notification sent");
        } else {
            Log.e(TAG, "No rain detected");
        }
        return Result.SUCCESS;
    }

    //Currently finds the first time it rains in the extended forecast. Also ExtendedForecast contains
    //information on whether or not to check for rain. Consider changing to something better and less stupid.
    //TODO: find multiple instances of rain. Group together long instances of rain (multiple hours).
    private HashMap<String, String> findRainMap() throws Exception {
        WeatherStation mWeatherStation = WeatherStation.get(getContext());
        HashMap<String, String> rainMap = new HashMap<>();

        mWeathers = mWeatherStation.getWeathers();
        for (Weather w : mWeathers) {
            Weather.ExtendedForecast extendedForecast = w.getExtendedForecast();

            if (extendedForecast.isNotifyReady()) {
                String rainTime = findRain(w);
                if (rainTime != null && rainTime.length() > 0) {
                    rainMap.put(w.getName(), rainTime);
                } else {
                    Log.i(TAG, "No rain detected for " + w.getName());
                }
            }
        }
        return rainMap;
    }

    private String findRain(Weather weather) throws Exception {
        int numberOfDays = 2;
        List<HourlyData> hourlyDataList = null;
        String rainStartTime;
        WeatherStation mWeatherStation = WeatherStation.get(getContext());

        if (weather != null) {
            weather = mWeatherStation.getExtendedWeather(weather);
            Weather.ExtendedForecast extendedForecast = weather.getExtendedForecast();
            hourlyDataList = extendedForecast.getHourlyDataList();
        }

        //find rain in extended forecast
        if (hourlyDataList != null) {
            for (int i = 0; i < (8 * numberOfDays); i++) {
                if (hourlyDataList.get(i).getMainDescription().equals("Rain")) {
                    rainStartTime = hourlyDataList.get(i).getTime();
                    Log.e(TAG, "Rain detected at " + rainStartTime);
                    return rainStartTime;
                }
            }
        }

        return null;
    }

    public static void scheduleJob() {
        int jobId = new JobRequest.Builder(WeatherFetchJob.TAG)
                .setPeriodic(TimeUnit.HOURS.toMillis(12), TimeUnit.HOURS.toMillis(1))
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .build()
                .schedule();
    }

    public static void scheduleJobOnce() {
        int jobId = new JobRequest.Builder(WeatherFetchJob.TAG)
                .startNow()
                .build()
                .schedule();
    }

    public void cancelJob(int jobId) {
        JobManager.instance().cancel(jobId);
    }

}
