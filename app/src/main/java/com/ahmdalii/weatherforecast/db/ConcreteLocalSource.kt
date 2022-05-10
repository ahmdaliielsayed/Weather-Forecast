package com.ahmdalii.weatherforecast.db

import android.content.Context
import androidx.lifecycle.LiveData
import com.ahmdalii.weatherforecast.model.Alert
import com.ahmdalii.weatherforecast.model.WeatherModel

class ConcreteLocalSource(context: Context) : LocalSource {

    private val dao: WeatherModelDAO?
    override val allStoredWeatherModel: LiveData<WeatherModel>
    override val allStoredAlertList: LiveData<List<Alert>>

    init {
        val db: AppDataBase = AppDataBase.getInstance(context)
        dao = db.weatherModelDAO()
        allStoredWeatherModel = dao?.weatherModel!!
        allStoredAlertList = dao.alertList
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