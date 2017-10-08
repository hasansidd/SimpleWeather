package com.siddapps.android.simpleweather;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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

import com.siddapps.android.simpleweather.WeatherJobs.WeatherFetchJob;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class WeatherFragment extends Fragment {
    private static final String TAG = "WeatherFragment";
    private WeatherStation mWeatherStation;
    private RecyclerView mRecyclerView;
    private WeatherAdapter mAdapter;
    private Callbacks mCallbacks;
    private FloatingActionButton mAddCityFAB;
    private Observer<Weather> updateUIObserver;

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

        MenuItem tempSetting = menu.findItem(R.id.temperature_setting);
        if (MainActivity.TEMPERATURE_SETTING == "F") {
            tempSetting.setTitle("Units (°C)");
        } else {
            tempSetting.setTitle("Units (°F)");
        }
        updateUI();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.temperature_setting:
                WeatherFetchJob.scheduleJob();
                if (MainActivity.TEMPERATURE_SETTING == "F") {
                    MainActivity.TEMPERATURE_SETTING = "C";
                } else {
                    MainActivity.TEMPERATURE_SETTING = "F";
                }
                getActivity().invalidateOptionsMenu();
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
        updateWeathers();
    }

    public void updateWeathers() {
        Observable<Weather> updateWeathers = mWeatherStation.updateWeathersObservable();
        if (updateWeathers != null) {
            updateWeathers.subscribe(updateUIObserver);
        }
    }

    private void addNewWeather(final String source) {
        Observable<Weather> addNewWeather = mWeatherStation.addWeatherObservable(source);
        addNewWeather.subscribe(updateUIObserver);
    }

    private void addCurrentWeather() {
        Observable<Weather> addCurrentWeather = mWeatherStation.addCurrentWeatherObservable();
        if (addCurrentWeather != null) {
            addCurrentWeather.subscribe(updateUIObserver);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        mWeatherStation = WeatherStation.get(getActivity());

        updateUIObserver = new Observer<Weather>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Weather weather) {
                mWeatherStation.setWeather(weather);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: ", e);
            }

            @Override
            public void onComplete() {
                updateUI();
            }
        };

        try {
            List<String> cityNameSet = mWeatherStation.getSharedPreferences();
            for (int i = 0; i < cityNameSet.size(); i++) {
                Log.i(TAG, "Adding " + cityNameSet.get(i) + " from SharedPrefs");
                addNewWeather(cityNameSet.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        addCurrentWeather();

        mRecyclerView = (RecyclerView) v.findViewById(R.id.weather_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupItemTouchHelper();

        mAddCityFAB = (FloatingActionButton) v.findViewById(R.id.add_weather);
        mAddCityFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

        return v;
    }

    public class WeatherHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mCityNameText;
        private TextView mCurrentTempText;
        private TextView mHighTemp;
        private TextView mLowTemp;
        private TextView mCurrentDescriptionText;
        private ImageView mWeatherBackgroundImage;
        private TextView mTimeText;
        Weather mWeather;

        public WeatherHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.weather_item_recyclerview, parent, false));
            itemView.setOnClickListener(this);

            mCityNameText = (TextView) itemView.findViewById(R.id.city_name);
            mCurrentTempText = (TextView) itemView.findViewById(R.id.current_temp_text);
            mHighTemp = itemView.findViewById(R.id.temp_high_text);
            mLowTemp = itemView.findViewById(R.id.temp_low_text);
            mCurrentDescriptionText = (TextView) itemView.findViewById(R.id.weather_description_text);
            mWeatherBackgroundImage = (ImageView) itemView.findViewById(R.id.weather_background_image);
            mTimeText = (TextView) itemView.findViewById(R.id.weather_time_text);
        }

        public void bind(Weather weather) {
            mWeather = weather;
            mCityNameText.setText(mWeather.getName());
            mCurrentTempText.setText(mWeather.getTemp());
            mHighTemp.setText(mWeather.getTemp_max());
            mLowTemp.setText(mWeather.getTemp_min());
            mCurrentDescriptionText.setText(mWeather.getDetailedDescription());
            mWeatherBackgroundImage.setImageResource(mWeather.getIcon());
            mTimeText.setText(mWeather.getTime());
        }

        @Override
        public void onClick(View v) {
            mCallbacks.OnWeatherSelected(mWeather);
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
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                List<Weather> weathers = mWeatherStation.getWeathers();
                mWeatherStation.deleteWeather(weathers.get(viewHolder.getAdapterPosition()));
                updateUI();
            }
        };
        ItemTouchHelper iTH = new ItemTouchHelper(sITC);
        iTH.attachToRecyclerView(mRecyclerView);
    }

    public class FetchExtendedWeatherTask extends AsyncTask<Weather, Void, Weather> {

        @Override
        protected Weather doInBackground(Weather... sourceWeather) {
            Weather weather = null;

            try {
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
