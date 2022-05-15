package com.ahmdalii.weatherforecast.ui.home.repo

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.ahmdalii.weatherforecast.model.WeatherModel
import retrofit2.Response

interface HomeRepoInterface {

    fun saveUpdateLocation(context: Context)
    fun isNotificationChecked(context: Context, isChecked: Boolean)

    suspend fun getCurrentWeatherOverNetwork(context: Context): Response<WeatherModel>
    fun getCurrentLocation(context: Context): List<String>
    fun getCurrentTempMeasurementUnit(context: Context): String
    fun getWindSpeedMeasurementUnit(context: Context): String

    fun getAppSharedPref(context: Context): SharedPreferences
    fun isLocationSet(context: Context): Boolean

    fun firstTimeCompleted(context: Context)
    fun isFirstTimeCompleted(context: Context): Boolean

    fun getCurrentTimeZone(context: Context): String

    fun insertWeatherModel(weatherModel: WeatherModel)
    fun selectAllStoredWeatherModel(context: Context): LiveData<WeatherModel>

    fun setLocationMethod(context: Context, locationMethod: String)
}