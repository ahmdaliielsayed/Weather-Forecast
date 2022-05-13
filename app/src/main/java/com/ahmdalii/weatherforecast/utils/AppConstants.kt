package com.ahmdalii.weatherforecast.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.ahmdalii.weatherforecast.BuildConfig
import com.ahmdalii.weatherforecast.R
import java.text.SimpleDateFormat
import java.util.*

object AppConstants {

    const val SPLASH_TIME_OUT: Long = 5000

    const val SETTING_FILE: String = "setting file"
    const val LOCATION_LONGITUDE: String = "location longitude"
    const val LOCATION_LATITUDE: String = "location latitude"
    const val DEVICE_LONGITUDE: String = "device longitude"
    const val DEVICE_LATITUDE: String = "device latitude"
    const val CURRENT_DEVICE_LOCATION: String = "current device location"
    const val LOCATION_ADMIN_AREA: String = "location admin area"
    const val LOCATION_LOCALITY: String = "location locality"
    const val APPLICATION_LANGUAGE: String = "application language"
    const val APPLICATION_LANGUAGE_AR: String = "application language arabic"
    const val APPLICATION_LANGUAGE_EN: String = "application language english"
    const val MEASUREMENT_UNIT: String = "measurement unit"
    /*
    * default: kelvin ==> metre/sec
    * metric: Celsius ==> metre/sec
    * imperial: Fahrenheit ==> miles/hour
    * */
    const val MEASUREMENT_UNIT_STANDARD: String = ""
    const val MEASUREMENT_UNIT_METRIC: String = "metric"
    const val MEASUREMENT_UNIT_IMPERIAL: String = "imperial"
    const val NOTIFICATION: String = "notification"
    const val FIRST_TIME_COMPLETED: String = "fist time completed"
    const val CURRENT_TIMEZONE: String = "current timezone"
    const val LOCATION_METHOD_GPS: String = "GPS"
    const val LOCATION_METHOD_MAP: String = "Map"
    const val LOCATION_METHOD: String = "location method"
    const val WIND_SPEED_UNIT: String = "windSpeed unit"
    const val WIND_SPEED_UNIT_M_P_S: String = "meter per second"
    const val WIND_SPEED_UNIT_M_P_H: String = "mile per hour"

    const val BASE_URL: String = BuildConfig.BASE_URL
    const val IMG_URL: String = BuildConfig.IMG_URL
    const val WEATHER_APP_ID: String = BuildConfig.WEATHER_APP_ID

    fun showAlert(context: Context, title: Int, message: String, icon: Int) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok) { _, _ -> }
            .setIcon(icon)
            .show()
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateTime(dt: Int, pattern: String): String {
        val format = SimpleDateFormat(pattern)
        format.timeZone = TimeZone.getTimeZone("GMT+2")
        return format.format(Date(dt * 1000L))
    }
}