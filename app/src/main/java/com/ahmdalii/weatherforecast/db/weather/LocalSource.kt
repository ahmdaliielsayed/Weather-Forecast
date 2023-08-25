package com.ahmdalii.weatherforecast.db.weather

import androidx.lifecycle.LiveData
import com.ahmdalii.weatherforecast.model.WeatherModel

interface LocalSource {

    fun selectAllStoredWeatherModel(currentTimeZone: String): LiveData<WeatherModel>
    fun insertWeatherModel(weatherModel: WeatherModel)
}
