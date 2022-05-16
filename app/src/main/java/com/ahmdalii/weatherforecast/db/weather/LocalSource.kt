package com.ahmdalii.weatherforecast.db.weather

import androidx.lifecycle.LiveData
import com.ahmdalii.weatherforecast.model.Alert
import com.ahmdalii.weatherforecast.model.FavoritePlace
import com.ahmdalii.weatherforecast.model.WeatherModel

interface LocalSource {

    fun selectAllStoredWeatherModel(currentTimeZone: String): LiveData<WeatherModel>
    val allStoredAlertList: LiveData<List<Alert>>
    fun insertWeatherModel(weatherModel: WeatherModel)
    fun insertAlert(alert: Alert)
    fun deleteAlert(alert: Alert)
}