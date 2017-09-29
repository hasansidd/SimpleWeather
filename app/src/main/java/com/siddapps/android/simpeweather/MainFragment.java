package com.siddapps.android.simpeweather;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";
    private WeatherFetcher mWeatherFetcher;
    private WeatherStation mWeatherStation;
    private RecyclerView mRecyclerView;
    private WeatherAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.i(TAG, "onCreate() called");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.weather_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update_weather:
                updateWeathers();
                return true;
            case R.id.add_weather:
                View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_weather, (ViewGroup) getView(), false);
                final EditText input = (EditText) viewInflated.findViewById(R.id.dialog_add_weather);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Add a new city")
                        .setView(viewInflated)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        addNewWeather(input.getText().toString());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
                return true;
            default:
                return false;
        }
    }

    private void updateUI() {
        Log.i(TAG, "Updating UI");
        List<Weather> weathers = mWeatherStation.getWeathers();
        mWeatherStation.setSharedPreferences();

        if (mAdapter == null) {
            mAdapter = new WeatherAdapter(weathers);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setWeathers(weathers);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume() called");
        updateUI();
    }

    public void updateWeathers() {
        UpdateWeathersTask updateWeathersTask = new UpdateWeathersTask();
        updateWeathersTask.execute();
    }

    private void addNewWeather(String source) {
        FetchNewWeatherTask fetchNewWeatherTask = new FetchNewWeatherTask();
        fetchNewWeatherTask.execute(source);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        Log.i(TAG, "onCreateView() called");
        mWeatherStation = WeatherStation.get(getActivity());

        try {
            FetchSavedWeatherTask fetchSavedWeatherTask = new FetchSavedWeatherTask();
            fetchSavedWeatherTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mWeatherFetcher = new WeatherFetcher(getActivity());
        FetchCurrentWeatherTask fetchCurrentWeatherTask = new FetchCurrentWeatherTask();
        fetchCurrentWeatherTask.execute();

        mRecyclerView = (RecyclerView) v.findViewById(R.id.weather_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupItemTouchHelper();

        return v;
    }

    public class WeatherHolder extends RecyclerView.ViewHolder {
        private TextView mCityNameText;
        private TextView mCurrentTempText;
        private TextView mCurrentDescriptionText;
        private ImageView mWeatherBackgroundImage;
        private TextView mTimeText;

        public WeatherHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.weather_item_recyclerview, parent, false));

            mCityNameText = (TextView) itemView.findViewById(R.id.city_name);
            mCurrentTempText = (TextView) itemView.findViewById(R.id.weather_temp_text);
            mCurrentDescriptionText = (TextView) itemView.findViewById(R.id.weather_description_text);
            mWeatherBackgroundImage = (ImageView) itemView.findViewById(R.id.weather_background_image);
            mTimeText = (TextView) itemView.findViewById(R.id.weather_time_text);
        }

        public void bind(Weather weather) {
            mCityNameText.setText(weather.getName());
            mCurrentTempText.setText(weather.getTemp());
            mCurrentDescriptionText.setText(weather.getDetailedDescription());
            mWeatherBackgroundImage.setImageResource(weather.getIcon());
            //Picasso.with(getActivity()).load(weather.getIcon()).into(mWeatherBackgroundImage);
            mWeatherBackgroundImage.setLayerType(View.LAYER_TYPE_SOFTWARE,null);
            mTimeText.setText(weather.formatTime(weather.getTime()));
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

    private void setupItemTouchHelper() {
        ItemTouchHelper.SimpleCallback sITC = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                Log.i(TAG, "moved");
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                List<Weather> weathers = mWeatherStation.getWeathers();
                Log.i(TAG, "moved: " + viewHolder.getAdapterPosition());
                mWeatherStation.deleteWeather(weathers.get(viewHolder.getAdapterPosition()));
                updateUI();
            }
        };
        ItemTouchHelper iTH = new ItemTouchHelper(sITC);
        iTH.attachToRecyclerView(mRecyclerView);
    }

    public class FetchCurrentWeatherTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                mWeatherStation.addCurrentWeather(mWeatherFetcher.getCurrentWeather());
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

    public class UpdateWeathersTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... zipCode) {
            List<Weather> weathers = mWeatherStation.getWeathers();
            try {
                for (Weather weather : weathers) {
                    mWeatherStation.updateWeather(weather);
                }
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

    public class FetchNewWeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... source) {

            try {
                Log.i(TAG, "Attempting to get weather for: " + source[0]);
                mWeatherStation.addWeather(mWeatherFetcher.getWeather(source[0]));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);
            updateUI();
        }
    }

    public class FetchSavedWeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... source) {

            try {
                Log.i(TAG, "getting shared preferences");
                mWeatherStation.getSharedPreferences();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);
            updateUI();
        }
    }

}
