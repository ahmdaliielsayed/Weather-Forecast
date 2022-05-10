package com.ahmdalii.weatherforecast.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ahmdalii.weatherforecast.model.Alert
import com.ahmdalii.weatherforecast.model.WeatherModel

@Dao
interface WeatherModelDAO {

    @get:Query("SELECT * FROM WeatherModel")
    val weatherModel: LiveData<WeatherModel>

    @get:Query("SELECT * FROM Alert")
    val alertList: LiveData<List<Alert>>

    /*@Query("SELECT * FROM movies WHERE title LIKE :movieTitle " + "LIMIT 1")
    fun findMovieByName(movieTitle: String): Movie*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWeatherModel(weatherModel: WeatherModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAlert(alert: Alert)

    @Delete
    fun deleteAlert(alert: Alert)
}