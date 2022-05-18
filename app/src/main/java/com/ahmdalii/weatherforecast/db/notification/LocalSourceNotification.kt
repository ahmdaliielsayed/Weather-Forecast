package com.ahmdalii.weatherforecast.db.notification

import androidx.lifecycle.LiveData
import com.ahmdalii.weatherforecast.model.MyAlert

interface LocalSourceNotification {

    val alertList: LiveData<List<MyAlert>>
    fun insertAlert(alert: MyAlert): Long
    fun deleteAlert(alert: MyAlert)
    fun deleteAlert(id: Long)
    fun getAlert(id: Long): MyAlert
}