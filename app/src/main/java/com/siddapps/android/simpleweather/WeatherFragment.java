package com.siddapps.android.simpleweather;

import android.app.AlertDialog;
import android.content.Context;
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

import java.util.List;


public class WeatherFragment extends Fragment {
    private static final String TAG = "WeatherFragment";
    private WeatherStation mWeatherStation;
    private RecyclerView mRecyclerView;
    private WeatherAdapter mAdapter;
    private Callbacks mCallbacks;

    public interface Callbacks {
        void OnWeatherSelected(Weather weather);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        mWeatherStation = WeatherStation.get(getActivity());

        try {
            List<String> cityNameSet = mWeatherStation.getSharedPreferences();
            for (int i = 0; i<cityNameSet.size(); i++) {
                Log.i(TAG, "Adding " + cityNameSet.get(i) + " from SharedPrefs");
                addNewWeather(cityNameSet.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        FetchCurrentWeatherTask fetchCurrentWeatherTask = new FetchCurrentWeatherTask();
        fetchCurrentWeatherTask.execute();

        mRecyclerView = (RecyclerView) v.findViewById(R.id.weather_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupItemTouchHelper();

        return v;
    }

    public class WeatherHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mCityNameText;
        private TextView mCurrentTempText;
        private TextView mCurrentDescriptionText;
        private ImageView mWeatherBackgroundImage;
        private TextView mTimeText;
        Weather mWeather;

        public WeatherHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.weather_item_recyclerview, parent, false));
            itemView.setOnClickListener(this);

            mCityNameText = (TextView) itemView.findViewById(R.id.city_name);
            mCurrentTempText = (TextView) itemView.findViewById(R.id.weather_temp_text);
            mCurrentDescriptionText = (TextView) itemView.findViewById(R.id.weather_description_text);
            mWeatherBackgroundImage = (ImageView) itemView.findViewById(R.id.weather_background_image);
            mTimeText = (TextView) itemView.findViewById(R.id.weather_time_text);
        }

        public void bind(Weather weather) {
            mWeather = weather;
            mCityNameText.setText(mWeather.getName());
            mCurrentTempText.setText(mWeather.getTemp());
            mCurrentDescriptionText.setText(mWeather.getDetailedDescription());
            mWeatherBackgroundImage.setImageResource(mWeather.getIcon());
            mTimeText.setText(mWeather.getTime());
        }

        @Override
        public void onClick(View v) {
            mCallbacks.OnWeatherSelected(mWeather);
            if (!mWeather.isExtendedForecastReady()) {

            }
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

    public class FetchCurrentWeatherTask extends AsyncTask<String, Void, Weather> {
        Weather weather = null;
        @Override
        protected Weather doInBackground(String... params) {
            try {
                weather=  mWeatherStation.addCurrentWeather();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return weather;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);
            updateUI();
            FetchExtendedWeatherTask fetchExtendedWeatherTask = new FetchExtendedWeatherTask();
            fetchExtendedWeatherTask.execute(weather);
        }
    }

    public class UpdateWeathersTask extends AsyncTask<String, Void, Weather> {
        Weather weather = null;

        @Override
        protected Weather doInBackground(String... zipCode) {
            List<Weather> weathers = mWeatherStation.getWeathers();
            try {
                for (Weather weather : weathers) {
                    weather = mWeatherStation.updateWeather(weather);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return weather;
        }

    @Override
    protected void onPostExecute(Weather weather) {
        super.onPostExecute(weather);
        updateUI();
        FetchExtendedWeatherTask fetchExtendedWeatherTask = new FetchExtendedWeatherTask();
        fetchExtendedWeatherTask.execute(weather);
    }
}

    public class FetchNewWeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... source) {
            Weather weather = null;
            try {
                Log.i(TAG, "Attempting to get weather for: " + source[0]);
                weather =  mWeatherStation.addWeather(source[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return weather;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);
            updateUI();
            FetchExtendedWeatherTask fetchExtendedWeatherTask = new FetchExtendedWeatherTask();
            fetchExtendedWeatherTask.execute(weather);
        }
    }

    public class FetchExtendedWeatherTask extends AsyncTask<Weather, Void, Weather> {

        @Override
        protected Weather doInBackground(Weather... sourceWeather) {
            Weather weather = null;

            try {
                Log.i(TAG, "getting extended weather");
                weather = mWeatherStation.getExtendedWeather(sourceWeather[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return weather;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);

        }
    }
}
