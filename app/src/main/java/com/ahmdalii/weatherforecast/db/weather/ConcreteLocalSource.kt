package com.ahmdalii.weatherforecast.db.weather

import android.content.Context
import androidx.lifecycle.LiveData
import com.ahmdalii.weatherforecast.db.AppDataBase
import com.ahmdalii.weatherforecast.model.Alert
import com.ahmdalii.weatherforecast.model.FavoritePlace
import com.ahmdalii.weatherforecast.model.WeatherModel

class ConcreteLocalSource(context: Context) : LocalSource {

    private val dao: WeatherModelDAO?
    override val allStoredAlertList: LiveData<List<Alert>>

    init {
        val db: AppDataBase = AppDataBase.getInstance(context)
        dao = db.weatherModelDAO()
        allStoredAlertList = dao?.alertList!!
    }

    override fun selectAllStoredWeatherModel(currentTimeZone: String): LiveData<WeatherModel> {
        return dao?.selectWeatherModel(currentTimeZone)!!
    }

    override fun insertWeatherModel(weatherModel: WeatherModel) {
        dao?.insertWeatherModel(weatherModel)
    }

    override fun insertAlert(alert: Alert) {
        dao?.insertAlert(alert)
    }

    override fun deleteAlert(alert: Alert) {
        dao?.deleteAlert(alert)
    }
}