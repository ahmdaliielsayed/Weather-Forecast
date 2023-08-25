package com.ahmdalii.weatherforecast.db.weather

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ahmdalii.weatherforecast.db.AppDataBase
import com.ahmdalii.weatherforecast.model.WeatherModel

class ConcreteLocalSource(context: Context) : LocalSource {

    private val dao: WeatherModelDAO?

    init {
        val db: AppDataBase = AppDataBase.getInstance(context)
        dao = db.weatherModelDAO()
    }

    override fun selectAllStoredWeatherModel(currentTimeZone: String): LiveData<WeatherModel> {
        return dao?.selectWeatherModel(currentTimeZone) ?: MutableLiveData()
    }

    override fun insertWeatherModel(weatherModel: WeatherModel) {
        dao?.insertWeatherModel(weatherModel)
    }
}
