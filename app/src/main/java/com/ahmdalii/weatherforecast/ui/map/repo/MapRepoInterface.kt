package com.ahmdalii.weatherforecast.ui.map.repo

import android.content.Context
import android.content.SharedPreferences
import android.location.Address
import android.location.Location
import androidx.lifecycle.LiveData
import com.ahmdalii.weatherforecast.model.WeatherModel
import retrofit2.Response

interface MapRepoInterface {

    fun getDeviceLocation(context: Context)

    fun getAppSharedPref(context: Context): SharedPreferences
    fun isLocationSet(context: Context, key: String): Boolean
    fun getLocationLatitude(context: Context): Float
    fun getLocationLongitude(context: Context): Float
    fun getCurrentDeviceLocation(context: Context): Location
    fun getCurrentAddress(context: Context, searchForPlace: String): Address

    /*fun saveUpdateLocation(context: Context)
    fun isNotificationChecked(context: Context, isChecked: Boolean)

    suspend fun getCurrentWeatherOverNetwork(context: Context): Response<WeatherModel>
    fun getCurrentTempMeasurementUnit(context: Context): String

    fun firstTimeCompleted(context: Context)
    fun isFirstTimeCompleted(context: Context): Boolean

    fun getCurrentTimeZone(context: Context): String

    fun insertWeatherModel(weatherModel: WeatherModel)
    fun selectAllStoredWeatherModel(context: Context): LiveData<WeatherModel>

    fun setLocationMethod(context: Context, locationMethod: String)*/
}