package com.ahmdalii.weatherforecast.ui.home.repo

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.db.weather.LocalSource
import com.ahmdalii.weatherforecast.model.WeatherModel
import com.ahmdalii.weatherforecast.network.RemoteSource
import com.ahmdalii.weatherforecast.utils.AppConstants.APPLICATION_LANGUAGE
import com.ahmdalii.weatherforecast.utils.AppConstants.CURRENT_TIMEZONE
import com.ahmdalii.weatherforecast.utils.AppConstants.FIRST_TIME_COMPLETED
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_ADMIN_AREA
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_LATITUDE
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_LOCALITY
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_LONGITUDE
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_METHOD
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_METHOD_GPS
import com.ahmdalii.weatherforecast.utils.AppConstants.MEASUREMENT_UNIT
import com.ahmdalii.weatherforecast.utils.AppConstants.MEASUREMENT_UNIT_IMPERIAL
import com.ahmdalii.weatherforecast.utils.AppConstants.MEASUREMENT_UNIT_METRIC
import com.ahmdalii.weatherforecast.utils.AppConstants.MEASUREMENT_UNIT_STANDARD
import com.ahmdalii.weatherforecast.utils.AppConstants.NOTIFICATION
import com.ahmdalii.weatherforecast.utils.AppConstants.SETTING_FILE
import com.ahmdalii.weatherforecast.utils.AppConstants.WIND_SPEED_UNIT
import com.ahmdalii.weatherforecast.utils.AppConstants.WIND_SPEED_UNIT_M_P_H
import com.ahmdalii.weatherforecast.utils.AppConstants.WIND_SPEED_UNIT_M_P_S
import com.ahmdalii.weatherforecast.utils.AppConstants.convertWindSpeedToMPH
import com.ahmdalii.weatherforecast.utils.AppConstants.convertWindSpeedToMPS
import com.ahmdalii.weatherforecast.utils.AppConstants.getDisplayCurrentLanguage
import com.ahmdalii.weatherforecast.utils.AppConstants.getPlaceName
import com.ahmdalii.weatherforecast.utils.AppSharedPref
import com.google.android.gms.location.*
import retrofit2.Response

class HomeRepo private constructor(
    private var remoteSource: RemoteSource,
    private var localSource: LocalSource
) : HomeRepoInterface {

    companion object {
        private var instance: HomeRepoInterface? = null
        fun getInstance(remoteSource: RemoteSource, localSource: LocalSource): HomeRepoInterface {
            return instance ?: HomeRepo(remoteSource, localSource)
        }
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun saveUpdateLocation(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setupLocation(context)

            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun setupLocation(context: Context) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        setupLocationRequest()
        setupLocationCallback(context)
    }

    private fun setupLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
    }

    private fun setupLocationCallback(context: Context) {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    if (location != null) {
                        saveLocationData(context, location)
                        val placeName = getPlaceName(context, location.latitude, location.longitude)
                        saveCurrentPlaceName(context, placeName)
                        stopLocationUpdates()
                    }
                }
            }
        }
    }

    private fun saveCurrentPlaceName(context: Context, address: Address) {
        AppSharedPref.getInstance(context, SETTING_FILE)
            .setValue(LOCATION_ADMIN_AREA, address.adminArea ?: context.getString(R.string.unknown_adminArea))
        AppSharedPref.getInstance(context, SETTING_FILE)
            .setValue(LOCATION_LOCALITY, address.locality  ?: context.getString(R.string.unknown_locality))
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    private fun saveLocationData(context: Context, location: Location) {
        AppSharedPref.getInstance(context, SETTING_FILE)
            .setValue(LOCATION_LATITUDE, location.latitude.toFloat())
        AppSharedPref.getInstance(context, SETTING_FILE)
            .setValue(LOCATION_LONGITUDE, location.longitude.toFloat())
    }

    override fun isNotificationChecked(context: Context, isChecked: Boolean) {
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(NOTIFICATION, isChecked)
    }

    override suspend fun getCurrentWeatherOverNetwork(context: Context): Response<WeatherModel> {
        val latitude =
            AppSharedPref.getInstance(context, SETTING_FILE).getFloatValue(LOCATION_LATITUDE)
        val longitude =
            AppSharedPref.getInstance(context, SETTING_FILE).getFloatValue(LOCATION_LONGITUDE)
        if (AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(LOCATION_METHOD, LOCATION_METHOD_GPS) == LOCATION_METHOD_GPS) {
            saveUpdateLocation(context)
        } else {
            val placeName = getPlaceName(context, latitude.toDouble(), longitude.toDouble())
            saveCurrentPlaceName(context, placeName)
        }
        val language =
            AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(APPLICATION_LANGUAGE, getDisplayCurrentLanguage())

        val measurementUnit = getCurrentTempMeasurementUnit(context)

        val currentWeatherOverNetwork = remoteSource.getCurrentWeatherOverNetwork(
            latitude,
            longitude,
            language,
            measurementUnit
        )
        val windSpeedUnit = AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(WIND_SPEED_UNIT, WIND_SPEED_UNIT_M_P_S)
        val measurementUnitValue = AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(MEASUREMENT_UNIT, MEASUREMENT_UNIT_STANDARD)
        if (measurementUnitValue == MEASUREMENT_UNIT_IMPERIAL && windSpeedUnit == WIND_SPEED_UNIT_M_P_S) {
            currentWeatherOverNetwork.body()!!.current.windSpeed =
                convertWindSpeedToMPS(currentWeatherOverNetwork.body()!!.current.windSpeed)
        } else if ((measurementUnitValue == MEASUREMENT_UNIT_METRIC && windSpeedUnit == WIND_SPEED_UNIT_M_P_H) ||
            (measurementUnitValue == MEASUREMENT_UNIT_STANDARD && windSpeedUnit == WIND_SPEED_UNIT_M_P_H)) {
            currentWeatherOverNetwork.body()!!.current.windSpeed = convertWindSpeedToMPH(currentWeatherOverNetwork.body()!!.current.windSpeed)
        }
        saveCurrentTimeZone(context, currentWeatherOverNetwork.body()!!.timezone)
        return currentWeatherOverNetwork
    }

    private fun saveCurrentTimeZone(context: Context, timeZone: String) {
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(CURRENT_TIMEZONE, timeZone)
    }

    override fun getCurrentLocation(context: Context): List<String> {
        return listOf(
            AppSharedPref.getInstance(context, SETTING_FILE)
                .getStringValue(LOCATION_ADMIN_AREA, ""),
            AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(LOCATION_LOCALITY, "")
        )
    }

    override fun getCurrentTempMeasurementUnit(context: Context): String {
        return AppSharedPref.getInstance(context, SETTING_FILE)
            .getStringValue(MEASUREMENT_UNIT, MEASUREMENT_UNIT_STANDARD)
    }

    override fun getWindSpeedMeasurementUnit(context: Context): String {
        return AppSharedPref.getInstance(context, SETTING_FILE)
            .getStringValue(WIND_SPEED_UNIT, WIND_SPEED_UNIT_M_P_S)
    }

    override fun getAppSharedPref(context: Context): SharedPreferences {
        return AppSharedPref.getInstance(context, SETTING_FILE).getAppSharedPref()
    }

    override fun isLocationSet(context: Context): Boolean {
        return AppSharedPref.getInstance(context, SETTING_FILE)
            .getFloatValue(LOCATION_LONGITUDE) != 0f
    }

    override fun firstTimeCompleted(context: Context) {
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(FIRST_TIME_COMPLETED, true)
    }

    override fun isFirstTimeCompleted(context: Context): Boolean {
        return AppSharedPref.getInstance(context, SETTING_FILE)
            .getBooleanValue(FIRST_TIME_COMPLETED, false)
    }

    override fun getCurrentTimeZone(context: Context): String {
        return AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(CURRENT_TIMEZONE, "")
    }

    override fun insertWeatherModel(weatherModel: WeatherModel) {
        localSource.insertWeatherModel(weatherModel)
    }

    override fun selectAllStoredWeatherModel(context: Context): LiveData<WeatherModel> {
        return localSource.selectAllStoredWeatherModel(getCurrentTimeZone(context))
    }

    override fun setLocationMethod(context: Context, locationMethod: String) {
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(LOCATION_METHOD, locationMethod)
    }

    override fun getLanguage(context: Context): String {
        return AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(APPLICATION_LANGUAGE, getDisplayCurrentLanguage())
    }
}