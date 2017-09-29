package com.siddapps.android.simpeweather;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;


public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";
    private WeatherFetcher mWeatherFetcher;
    private WeatherStation mWeatherStation;
    private RecyclerView mRecyclerView;
    private WeatherAdapter mAdapter;

    private void updateUI() {
        WeatherStation weatherStation = WeatherStation.get(getActivity());
        List<Weather> weathers = weatherStation.getWeathers();


        if (mAdapter == null) {
            mAdapter = new WeatherAdapter(weathers);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setWeathers(weathers);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        mWeatherStation = WeatherStation.get(getActivity());

        mWeatherFetcher = new WeatherFetcher(getActivity());
        FetchCurrentWeatherTask fetchCurrentWeatherTask = new FetchCurrentWeatherTask();
        fetchCurrentWeatherTask.execute();

        mRecyclerView = (RecyclerView) v.findViewById(R.id.weather_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();
        return v;
    }

    public class WeatherHolder extends RecyclerView.ViewHolder {
        private TextView mCityNameText;
        private TextView mCurrentTempText;
        private TextView mCurrentDescriptionText;
        private ImageView mWeatherBackgroundImage;
        Weather mWeather;

        public WeatherHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.weather_item_recyclerview, parent, false));

            mCityNameText = (TextView) itemView.findViewById(R.id.city_name);
            mCurrentTempText = (TextView) itemView.findViewById(R.id.weather_temp_text);
            mCurrentDescriptionText = (TextView) itemView.findViewById(R.id.weather_description_text);
            mWeatherBackgroundImage = (ImageView) itemView.findViewById(R.id.weather_background_image);
        }

        public void bind(Weather weather) {
            mWeather = weather;

            mCityNameText.setText(mWeather.getName());
            String temp = String.format("%s°F", mWeather.getTemp());
            mCurrentTempText.setText(temp);
            mCurrentDescriptionText.setText(mWeather.getDetailedDescription());
            Log.i(TAG, String.valueOf(mWeather.getIcon()));
            Picasso.with(getActivity()).load(mWeather.getIcon()).into(mWeatherBackgroundImage);
        }
    }

    public class WeatherAdapter extends RecyclerView.Adapter<WeatherHolder> {
        private List<Weather> mWeathers;

        public WeatherAdapter(List<Weather> weathers) {
            mWeathers = weathers;
        }

        @Override
        public WeatherHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new WeatherHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(WeatherHolder holder, int position) {
            Weather weather = mWeathers.get(position);
            holder.bind(weather);
        }

        @Override
        public int getItemCount() {
            return mWeathers.size();
        }

        public void setWeathers(List<Weather> weathers) {
            mWeathers = weathers;
        }
    }

    public class FetchCurrentWeatherTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                mWeatherStation.addCurrentWeather(mWeatherFetcher.getCurrentWeather());
                mWeatherStation.addWeather(mWeatherFetcher.getWeather("71111"));
                mWeatherStation.addWeather(mWeatherFetcher.getWeather("78628"));
                mWeatherStation.addWeather(mWeatherFetcher.getWeather("78628"));
                mWeatherStation.addWeather(mWeatherFetcher.getWeather("75219"));
                mWeatherStation.addWeather(mWeatherFetcher.getWeather("77004"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateUI();
        }
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... zipCode) {
            try {
                mWeatherStation.addWeather(mWeatherFetcher.getWeather("71111"));
                mWeatherStation.addWeather(mWeatherFetcher.getWeather("78628"));
                mWeatherStation.addWeather(mWeatherFetcher.getWeather("78628"));
                mWeatherStation.addWeather(mWeatherFetcher.getWeather("75219"));
                mWeatherStation.addWeather(mWeatherFetcher.getWeather("77004"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateUI();
        }
    }

}
