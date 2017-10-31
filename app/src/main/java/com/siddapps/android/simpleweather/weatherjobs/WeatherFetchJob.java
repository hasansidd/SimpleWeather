package com.siddapps.android.simpleweather.weatherjobs;

import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.siddapps.android.simpleweather.R;
import com.siddapps.android.simpleweather.data.Weather;
import com.siddapps.android.simpleweather.data.Weather.ExtendedForecast.HourlyData;
import com.siddapps.android.simpleweather.data.WeatherStation;
import com.siddapps.android.simpleweather.weather.WeatherActivity;
import com.siddapps.android.simpleweather.weatherdetail.WeatherDetailActivity;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WeatherFetchJob extends Job {

    public static final String TAG = "WeatherFetchJob";
    private final static String GROUP_KEY = "weathernotification";
    public List<Weather> mWeathers;

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        NotificationManagerCompat notificationManager =  NotificationManagerCompat.from(getContext());
        Log.e(TAG, "running");

        HashMap<String, String> rainMap = new HashMap<>();
        try {
            rainMap = findRainMap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (rainMap != null && rainMap.size() > 0) {
            for (int i = 0; i < rainMap.keySet().size(); i++) {
                String key = (String) rainMap.keySet().toArray()[i];
                String formattedText = (getContext().getString(R.string.weather_alert_content_individual, rainMap.get(key)));

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "main")
                        .setContentTitle(key)
                        .setContentText(formattedText)
                        .setAutoCancel(true)
                        .setContentIntent(PendingIntent.getActivity(getContext(), i, WeatherDetailActivity.newIntent(getContext(),key), 0))
                        .setSmallIcon(R.drawable.notification)
                        .setShowWhen(true)
                        .setColor(getContext().getResources().getColor(R.color.colorAccent))
                        .setLocalOnly(true)
                        .setGroup(GROUP_KEY);

                notificationManager.notify(i,builder.build());
            }

            getNotificationSummary(rainMap);

            //https://github.com/evernote/android-job/issues/298
            //If the job is ran from scheduleJobOnce(), this will set it to run periodically
            if (JobManager.instance().getAllJobRequests().size() == 0) {
                scheduleJobPeriodic();
            }

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
        HashMap<String, String> rainMap = new HashMap<>();
        mWeathers = WeatherStation.get().getSharedPreferences(getContext());

        for (Weather w : mWeathers) {
            Weather.ExtendedForecast extendedForecast = w.getExtendedForecast();

            if (extendedForecast.isNotifyReady()) {
                String rainTime = findRain(w);

                if (rainTime != null && rainTime.length() > 0) {
                    rainMap.put(w.getName(), rainTime);
                } else {
                    Log.d(TAG, "No rain detected for " + w.getName());
                }

            }
        }
        return rainMap;
    }

    private String findRain(Weather weather) throws Exception {
        int numberOfDays = getAlertRange();
        List<HourlyData> hourlyDataList;
        String rainStartTime;

        if (weather != null) {
            weather = WeatherStation.get().getExtendedWeather(weather);
            hourlyDataList = weather.getExtendedForecast().getHourlyDataList();

            //find rain in extended forecast
            if (hourlyDataList.size() >= (8 * numberOfDays)) {
                for (int i = 0; i < (8 * numberOfDays); i++) {
                    if (hourlyDataList.get(i).getMainDescription().equals("Rain")) {
                        rainStartTime = hourlyDataList.get(i).getTime();
                        Log.d(TAG, "Rain detected in " + weather.getName() + " at " + rainStartTime);
                        return rainStartTime;
                    }
                }
            }
        }

        return null;
    }

    private int getAlertRange() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String numberOfDaysString = sharedPreferences.getString(getContext().getString(R.string.pref_alert_range_key), getContext().getString(R.string.pref_alert_range_default));
        int numberOfDays = Integer.parseInt(numberOfDaysString);
        return numberOfDays;
    }

    private void getNotificationSummary(HashMap<String, String> rainMap){
        NotificationManagerCompat notificationManager =  NotificationManagerCompat.from(getContext());

        String formattedTitle;
        if (rainMap.size() == 1) {
            formattedTitle = getContext().getString(R.string.weather_alert_title_single);
        } else {
            formattedTitle = (getContext().getString(R.string.weather_alert_title, rainMap.size()));
        }

        String formattedText="";
        for (String key: rainMap.keySet()){
            formattedText += getContext().getString(R.string.weather_alert_content_summnary, key, rainMap.get(key));
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "main")
                .setContentTitle(formattedTitle)
                .setContentText(formattedText)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(getContext(), -1, WeatherActivity.newIntent(getContext()), 0))
                .setSmallIcon(R.drawable.notification)
                .setShowWhen(true)
                .setColor(getContext().getResources().getColor(R.color.colorAccent))
                .setLocalOnly(true)
                .setGroup(GROUP_KEY)
                .setGroupSummary(true);

        notificationManager.notify(-1,builder.build());
    }

    public static void scheduleJobPeriodic() {
        int jobId = new JobRequest.Builder(WeatherFetchJob.TAG)
                .setPeriodic(TimeUnit.HOURS.toMillis(12), TimeUnit.HOURS.toMillis(1))
                .setUpdateCurrent(true)
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
}
