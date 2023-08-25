package com.ahmdalii.weatherforecast.ui.notification.repo

import android.content.Context
import androidx.lifecycle.LiveData
import com.ahmdalii.weatherforecast.model.MyAlert
import com.ahmdalii.weatherforecast.model.WeatherModel

interface NotificationRepoInterface {

    val alertList: LiveData<List<MyAlert>>
    fun insertAlert(alert: MyAlert): Long
    fun deleteAlert(alert: MyAlert)
    fun deleteAlert(id: Long)
    fun getAlert(id: Long): MyAlert
    fun selectAllStoredWeatherModel(context: Context): LiveData<WeatherModel>

    fun getLanguage(context: Context): String
    fun getNotificationChecked(context: Context): Boolean
}
