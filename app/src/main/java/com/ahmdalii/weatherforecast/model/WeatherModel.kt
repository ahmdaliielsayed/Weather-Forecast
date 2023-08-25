package com.ahmdalii.weatherforecast.model

import androidx.annotation.Nullable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "WeatherModel")
data class WeatherModel(
    @PrimaryKey
    @ColumnInfo(name = "timezone")
    val timezone: String,

    @ColumnInfo(name = "timezone_offset")
    @SerializedName("timezone_offset")
    val timezoneOffset: Int,

    @ColumnInfo(name = "lat")
    val lat: Double,

    @ColumnInfo(name = "lon")
    val lon: Double,

    @Nullable
    @ColumnInfo(name = "alerts")
    val alerts: List<Alert>? = emptyList(),

    @ColumnInfo(name = "current")
    val current: Current,

    @ColumnInfo(name = "daily")
    val daily: List<Daily>? = emptyList(),

    @ColumnInfo(name = "hourly")
    var hourly: List<Hourly>? = emptyList(),

    @Nullable
    @ColumnInfo(name = "minutely")
    val minutely: List<Minutely>? = emptyList(),
)
