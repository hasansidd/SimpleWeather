package com.siddapps.android.simpleweather.weather;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.siddapps.android.simpleweather.R;
import com.siddapps.android.simpleweather.data.model.Weather;
import com.siddapps.android.simpleweather.data.WeatherStation;
import com.siddapps.android.simpleweather.settings.SettingsActivity;
import com.siddapps.android.simpleweather.views.WeatherView;
import com.siddapps.android.simpleweather.privacypolicy.PrivacyPolicyActivity;

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
    private Observer<Weather> addCurrentObserver;
    Observable<Weather> addCurrentWeather;
    Observable<Weather> addNewWeather;
    Observable<Weather> updateWeathers;
    public int indextester = 0;

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
            case R.id.settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            case R.id.privacy_policy:
                startActivity(new Intent(getActivity(), PrivacyPolicyActivity.class));
            default:
                return false;
        }
    }

    private void updateUI() {
        Log.i(TAG, "Updating UI");
        List<Weather> weathers = mWeatherStation.getWeathers(getContext());

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
        //updateWeathers(getContext());
        addCurrentWeather();
    }

    private void updateWeathers(Context context) {
        updateWeathers = mWeatherStation.updateWeathersObservable(context);
        if (updateWeathers != null) {
            updateWeathers.subscribe(updateUIObserver);
        }
    }

    private void addNewWeather(final String source) {
        addNewWeather = mWeatherStation.addWeatherObservable(source, getActivity());
        addNewWeather.subscribe(updateUIObserver);
    }

    private void addCurrentWeather() {
        addCurrentWeather = mWeatherStation.addCurrentWeatherObservable(getActivity());
        if (addCurrentWeather != null) {
            addCurrentWeather.subscribe(addCurrentObserver);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        mWeatherStation = WeatherStation.get();

        updateUIObserver = new Observer<Weather>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Weather weather) {
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

        addCurrentObserver = new Observer<Weather>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(Weather weather) {
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "onError: ", e);
            }

            @Override
            public void onComplete() {
                updateWeathers(getContext());
            }
        };

        mRecyclerView = v.findViewById(R.id.weather_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        addCurrentWeather();
       // updateUI();

        setupItemTouchHelper();

        mAddCityFAB = v.findViewById(R.id.add_weather);
        mAddCityFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_weather, (ViewGroup) getView(), false);
                final EditText input = viewInflated.findViewById(R.id.dialog_add_weather);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(getString(R.string.add_new_city))
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
        private Weather mWeather;
        private WeatherView mWeatherView;

        public WeatherHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.weather_item_recyclerview, parent, false));
            itemView.setOnClickListener(this);
            mWeatherView = itemView.findViewById(R.id.weather_view);
        }

        public void bind(Weather weather) {
            mWeather = weather;
            mWeatherView.bindWeather(mWeather);
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
        ItemTouchHelper.SimpleCallback sITC = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                List<Weather> weathers = mWeatherStation.getWeathers(getContext());
                mWeatherStation.deleteWeather(weathers.get(viewHolder.getAdapterPosition()),getContext());
                updateUI();
            }
        };
        ItemTouchHelper iTH = new ItemTouchHelper(sITC);
        iTH.attachToRecyclerView(mRecyclerView);
    }
}
