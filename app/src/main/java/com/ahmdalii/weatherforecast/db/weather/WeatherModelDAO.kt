package com.ahmdalii.weatherforecast.db.weather

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ahmdalii.weatherforecast.model.WeatherModel

@Dao
interface WeatherModelDAO {

    @Query("SELECT * FROM WeatherModel WHERE timezone = :currentTimeZone")
    fun selectWeatherModel(currentTimeZone: String): LiveData<WeatherModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWeatherModel(weatherModel: WeatherModel)
}