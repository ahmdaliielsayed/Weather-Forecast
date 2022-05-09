package com.ahmdalii.weatherforecast.ui.home.repo

import android.content.Context
import android.content.SharedPreferences
import com.ahmdalii.weatherforecast.model.WeatherModel
import retrofit2.Response

interface HomeRepoInterface {

    fun saveUpdateLocation(context: Context)
    fun isNotificationChecked(context: Context, isChecked: Boolean)

    suspend fun getCurrentWeatherOverNetwork(context: Context): Response<WeatherModel>
    fun getCurrentLocation(context: Context): List<String>
    fun getCurrentTempMeasurementUnit(context: Context): String

    fun getAppSharedPref(context: Context): SharedPreferences
    fun isLocationSet(context: Context): Boolean

    fun firstTimeCompleted(context: Context)
    fun isFirstTimeCompleted(context: Context): Boolean
}