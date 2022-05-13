package com.ahmdalii.weatherforecast.ui.setting.repo

import android.content.Context
import android.content.SharedPreferences

interface SettingsRepoInterface {

    fun setLocationMethod(context: Context, locationMethod: String)
    fun getLocationMethod(context: Context): String

    fun setLanguage(context: Context, language: String)
    fun getLanguage(context: Context): String

    fun setCurrentTempMeasurementUnit(context: Context, measurementUnit: String)
    fun getCurrentTempMeasurementUnit(context: Context): String

    fun setWindSpeedUnit(context: Context, windSpeedUnit: String)
    fun getWindSpeedUnit(context: Context): String

    fun setNotificationChecked(context: Context, isChecked: Boolean)
    fun getNotificationChecked(context: Context): Boolean

    fun getAppSharedPref(context: Context): SharedPreferences
}