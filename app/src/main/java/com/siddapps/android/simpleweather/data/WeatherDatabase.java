package com.siddapps.android.simpleweather.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

import com.siddapps.android.simpleweather.data.model.HourlyData;
import com.siddapps.android.simpleweather.data.model.Weather;

@Database(entities = {Weather.class, HourlyData.class}, version = 2) //Entities listed here
public abstract class WeatherDatabase extends RoomDatabase {

    private static final String TAG = WeatherDatabase.class.getSimpleName();
    private static final String DATABASE_NAME = "weather";

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static volatile WeatherDatabase sInstance;
    
    public static WeatherDatabase getInstance(Context context) {
        Log.d(TAG, "Getting the database");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(), WeatherDatabase.class, WeatherDatabase.DATABASE_NAME).allowMainThreadQueries().fallbackToDestructiveMigration().build();
                Log.d(TAG, "Created new database");
            }
        }
        return sInstance;
    }

    public abstract WeatherDao weatherDao(); //Getters for Dao
}