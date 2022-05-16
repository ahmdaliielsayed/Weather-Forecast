package com.ahmdalii.weatherforecast.db.weather

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ahmdalii.weatherforecast.model.Alert
import com.ahmdalii.weatherforecast.model.FavoritePlace
import com.ahmdalii.weatherforecast.model.WeatherModel

@Dao
interface WeatherModelDAO {

    @Query("SELECT * FROM WeatherModel WHERE timezone = :currentTimeZone")
    fun selectWeatherModel(currentTimeZone: String): LiveData<WeatherModel>

    @get:Query("SELECT * FROM Alert")
    val alertList: LiveData<List<Alert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWeatherModel(weatherModel: WeatherModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAlert(alert: Alert)

    @Delete
    fun deleteAlert(alert: Alert)
}