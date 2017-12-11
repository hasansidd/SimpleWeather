package com.siddapps.android.simpleweather.weatherjobs;

import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.siddapps.android.simpleweather.R;
import com.siddapps.android.simpleweather.data.model.HourlyData;
import com.siddapps.android.simpleweather.data.model.Weather;
import com.siddapps.android.simpleweather.data.WeatherStation;
import com.siddapps.android.simpleweather.util.TimeUtil;
import com.siddapps.android.simpleweather.weather.WeatherActivity;
import com.siddapps.android.simpleweather.weatherdetail.WeatherDetailActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WeatherFetchJob extends Job {

    public static final String TAG = "WeatherFetchJob";
    private final static String GROUP_KEY = "weathernotification";
    public List<Weather> mWeathers;

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        Log.e(TAG, "running");

        List<WeatherNotification> weatherNotificationList = new ArrayList<>();
        try {
            weatherNotificationList = findBadWeatherMap();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (weatherNotificationList != null && weatherNotificationList.size() > 0) {
            for (int i = 0; i < weatherNotificationList.size(); i++) {
                String formattedText = (getContext().getString(R.string.weather_alert_content_individual, weatherNotificationList.get(i).getWeatherType(), weatherNotificationList.get(i).getTime()));

                //https://stackoverflow.com/questions/23328367/up-to-parent-activity-on-android
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(getContext());
                stackBuilder.addParentStack(WeatherDetailActivity.class);
                stackBuilder.addNextIntent(WeatherDetailActivity.newIntent(getContext(), weatherNotificationList.get(i).getCityName()));
                PendingIntent pi = stackBuilder.getPendingIntent(i, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "main")
                        .setContentTitle(weatherNotificationList.get(i).getCityName())
                        .setContentText(formattedText)
                        .setAutoCancel(true)
                        .setContentIntent(pi)
                        .setSmallIcon(R.drawable.notification)
                        .setShowWhen(true)
                        .setColor(getContext().getResources().getColor(R.color.colorAccent))
                        .setLocalOnly(true)
                        .setGroup(GROUP_KEY);

                notificationManager.notify(i, builder.build());
            }

            getNotificationSummary(weatherNotificationList);

            //https://github.com/evernote/android-job/issues/298
            //If the job is ran from scheduleJobOnce(), this will set it to run periodically
            if (JobManager.instance().getAllJobRequests().size() == 0) {
                scheduleJobPeriodic();
            }

            Log.d(TAG, "notification sent");
        } else {
            Log.d(TAG, "No rain detected");
        }

        return Result.SUCCESS;
    }

    //Currently finds the first time it rains in the extended forecast. Also ExtendedForecast contains
    //information on whether or not to check for rain. Consider changing to something better and less stupid.
    //TODO: find multiple instances of rain. Group together long instances of rain (multiple hours).
    private List<WeatherNotification> findBadWeatherMap() throws Exception {
        List<WeatherNotification> weatherNotificationList = new ArrayList<>();
        mWeathers = WeatherStation.get().getNotifyReadyWeathers(getContext());

        for (Weather w : mWeathers) {
            WeatherNotification weatherNotification = findBadWeather(w);

            if (weatherNotification != null) {
                weatherNotificationList.add(weatherNotification);
            } else {
                Log.d(TAG, "No bad weather detected for " + w.getName());
            }

        }
        return weatherNotificationList;
    }

    private WeatherNotification findBadWeather(Weather weather) throws Exception {
        int numberOfDays = getAlertRange();
        List<HourlyData> hourlyDataList;
        hourlyDataList = WeatherStation.get().getExtendedWeather(getContext(), weather);

        //find bad weather in extended forecast
        for (int i = 0; i < (8 * numberOfDays); i++) {
            if (hourlyDataList.get(i).getMainDescription().equals("Rain")) {
                Log.d(TAG, "Rain detected in " + weather.getName() + " at " + TimeUtil.formatTime(hourlyDataList.get(i).getTime()));

                WeatherNotification weatherNotification = new WeatherNotification(
                        weather.getName(),
                        TimeUtil.formatTime(hourlyDataList.get(i).getTime()),
                        "Rain");

                return weatherNotification;
            } else if (hourlyDataList.get(i).getMainDescription().equals("Snow")) {
                Log.d(TAG, "Snow detected in " + weather.getName() + " at " + TimeUtil.formatTime(hourlyDataList.get(i).getTime()));

                WeatherNotification weatherNotification = new WeatherNotification(
                        weather.getName(),
                        TimeUtil.formatTime(hourlyDataList.get(i).getTime()),
                        "Snow");

                return weatherNotification;
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

    private void getNotificationSummary(List<WeatherNotification> weatherNotificationList) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());

        String formattedTitle;
        if (weatherNotificationList.size() == 1) {
            formattedTitle = getContext().getString(R.string.weather_alert_title_single);
        } else {
            formattedTitle = (getContext().getString(R.string.weather_alert_title, weatherNotificationList.size()));
        }

        String formattedText = "";
        for (WeatherNotification w : weatherNotificationList) {
            formattedText += getContext().getString(R.string.weather_alert_content_summnary, w.getWeatherType(), w.getCityName(),w.getTime());
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

        notificationManager.notify(-1, builder.build());
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
