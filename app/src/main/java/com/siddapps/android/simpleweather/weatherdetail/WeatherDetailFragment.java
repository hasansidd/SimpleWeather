package com.siddapps.android.simpleweather.weatherdetail;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.siddapps.android.simpleweather.R;
import com.siddapps.android.simpleweather.data.Weather;
import com.siddapps.android.simpleweather.data.WeatherStation;
import com.siddapps.android.simpleweather.weatherjobs.WeatherFetchJob;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class WeatherDetailFragment extends Fragment {
    private static final String TAG = "WeatherDetailFragment";
    static String sCityName;
    private Weather mWeather;
    private TextView mCityNameText;
    private TextView mCurrentTempText;
    private TextView mHighTemp;
    private TextView mLowTemp;
    private TextView mDescriptionText;
    private TextView mCurrentTimeText;
    private ImageView mWeatherImage;
    private RecyclerView mRecyclerView;
    private WeatherDetailAdapter mAdapter;
    private WeatherStation mWeatherStation;
    private ImageView mWeatherAlertImage;
    private Disposable mDisposable;

    public static WeatherDetailFragment newInstance() {
        return new WeatherDetailFragment();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.weather_detail_menu, menu);

        MenuItem rainNotification = menu.findItem(R.id.rain_notification);
        Weather.ExtendedForecast extendedForecast = mWeather.getExtendedForecast();
        if (extendedForecast.isNotifyReady()) {
            rainNotification.setTitle(getString(R.string.disable_weather_notification));
        } else {
            rainNotification.setTitle(getString(R.string.enable_weather_notification));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rain_notification:
                Weather.ExtendedForecast extendedForecast = mWeather.getExtendedForecast();
                if (extendedForecast.isNotifyReady()) {
                    extendedForecast.setNotifyReady(false);
                    mWeatherStation.shouldCancelJobs();
                } else {
                    extendedForecast.setNotifyReady(true);
                    WeatherFetchJob.scheduleJobOnce();
                }
                mWeather.setExtendedForecast(extendedForecast);
                getActivity().invalidateOptionsMenu();
                updateUI();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mWeatherStation = WeatherStation.get();
        mWeather = mWeatherStation.getWeather(sCityName);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_weather_detail, container, false);
        mCityNameText = v.findViewById(R.id.master_city_name);
        mCurrentTempText = v.findViewById(R.id.master_temp_text);
        mHighTemp = v.findViewById(R.id.master_temp_high_text);
        mLowTemp = v.findViewById(R.id.master_temp_low_text);
        mDescriptionText = v.findViewById(R.id.master_description_text);
        mCurrentTimeText = v.findViewById(R.id.master_time_text);
        mWeatherImage = v.findViewById(R.id.master_background_image);
        mRecyclerView = v.findViewById(R.id.weather_detail_recyclerview);
        mWeatherAlertImage = v.findViewById(R.id.weather_alert_detail);


        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        getExtendedForecast();
        updateUI();
        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDisposable.dispose();
        Log.e(TAG, "disposed");
    }

    public class WeatherDetailHolder extends RecyclerView.ViewHolder {
        ImageView mWeatherImage;
        TextView mTempText;
        TextView mTimeText;

        public WeatherDetailHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.weather_detail_recyclerview, parent, false));

            mWeatherImage = itemView.findViewById(R.id.detail_weather_image);
            mTempText = itemView.findViewById(R.id.detail_temp);
            mTimeText = itemView.findViewById(R.id.detail_time_text);
        }

        public void bind(Weather.ExtendedForecast.HourlyData hourlyData) {
            mWeatherImage.setImageResource(hourlyData.getIcon());
            mTempText.setText(mWeatherStation.formatTemp(getActivity(), hourlyData.getTemp()));
            mTimeText.setText(hourlyData.getTime());
        }
    }

    public class WeatherDetailAdapter extends RecyclerView.Adapter<WeatherDetailHolder> {
        private List<Weather.ExtendedForecast.HourlyData> hourlyData;

        public WeatherDetailAdapter(Weather weather) {
            hourlyData = weather.getExtendedForecast().getHourlyDataList();
        }

        @Override
        public WeatherDetailHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new WeatherDetailHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(WeatherDetailHolder holder, int position) {
            holder.bind(hourlyData.get(position));
        }

        @Override
        public int getItemCount() {
            return hourlyData.size();
        }

        public void setHourlyData(Weather weather) {
            hourlyData = weather.getExtendedForecast().getHourlyDataList();
        }
    }

    private void getExtendedForecast() {
        Observable.defer(new Callable<ObservableSource<Weather>>() {
            @Override
            public ObservableSource<Weather> call() throws Exception {
                return Observable.just(mWeatherStation.getExtendedWeather(mWeather));
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .share()
                .subscribe(new Observer<Weather>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposable = d;
                    }

                    @Override
                    public void onNext(Weather weather) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ");
                    }

                    @Override
                    public void onComplete() {
                        updateUI();
                    }
                });
    }

    private void updateUI() {
        mWeatherStation.setSharedPreferences(getActivity());
        if (mWeather.isExtendedForecastReady()) {
            if (mAdapter == null) {
                mAdapter = new WeatherDetailAdapter(mWeather);
                mRecyclerView.setAdapter(mAdapter);
            } else {
                mAdapter.setHourlyData(mWeather);
                mAdapter.notifyDataSetChanged();
            }
        }

        mCityNameText.setText(mWeather.getName());
        mCurrentTempText.setText(mWeatherStation.formatTemp(getActivity(), mWeather.getTemp()));
        mHighTemp.setText(mWeatherStation.formatTemp(getActivity(), mWeather.getTemp_max()));
        mLowTemp.setText(mWeatherStation.formatTemp(getActivity(), mWeather.getTemp_min()));
        mDescriptionText.setText(mWeather.getDetailedDescription());
        mWeatherImage.setImageResource(mWeather.getIcon());
        mCurrentTimeText.setText(mWeather.getTime());
        //noinspection ResourceType
        mWeatherAlertImage.setVisibility(mWeather.getNotifyAlert());
    }
}
