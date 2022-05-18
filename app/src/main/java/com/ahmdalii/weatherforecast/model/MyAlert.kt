package com.ahmdalii.weatherforecast.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "MyAlerts")
data class MyAlert(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    var startDate: Long,
    var endDate: Long,
    var alarm_or_notification: String
)