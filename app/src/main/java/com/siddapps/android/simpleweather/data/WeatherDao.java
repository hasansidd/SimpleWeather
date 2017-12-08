package com.siddapps.android.simpleweather.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.siddapps.android.simpleweather.data.model.HourlyData;
import com.siddapps.android.simpleweather.data.model.Weather;

import java.util.List;

@Dao
public interface WeatherDao {

    @Query("SELECT * FROM weather ORDER BY current DESC")
    List<Weather> getWeathers();

    @Query("SELECT * FROM weather WHERE name IS :name")
    Weather getWeather(String name);

    @Query("SELECT * FROM weather WHERE notifyReady IS 1")
    List<Weather> getNotifyReadyWeathers();

    @Query("SELECT id FROM weather WHERE name IS :name")
    int getIdFromCityName(String name);

    @Query("SELECT notifyReady FROM weather WHERE name IS :name")
    boolean isNotifyReadyFromName(String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addWeather(Weather weather);

    @Update
    void updateWeathers(List<Weather> weathers);

    @Update
    void updateWeather(Weather weather);

    @Delete
    void deleteWeather(Weather weather);

    @Query("SELECT * FROM HourlyData WHERE name IS :name ORDER BY timeFetched DESC")
    List<HourlyData> getHourlyData(String name);

    @Insert
    void addHourlyData(List<HourlyData> hourlyDataList);

    @Query("DELETE FROM hourlydata WHERE name is :name")
    void deleteAllHourlyDataByCity(String name);
}
