package com.ahmdalii.weatherforecast.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "WeatherModel")
class WeatherModel {

    @PrimaryKey
    @ColumnInfo(name = "timezone")
    private lateinit var timezone: String

    @ColumnInfo(name = "timezone_offset")
    @SerializedName("timezone_offset")
    private var timezoneOffset: Int = 0

    @ColumnInfo(name = "lat")
    private var lat: Double = 0.0

    @ColumnInfo(name = "lon")
    private var lon: Double = 0.0

    @ColumnInfo(name = "alerts")
    private var alerts: List<Alert> = emptyList()

    @ColumnInfo(name = "current")
    private lateinit var current: Current

    @ColumnInfo(name = "daily")
    private var daily: List<Daily> = emptyList()

    @ColumnInfo(name = "hourly")
    private var hourly: List<Hourly> = emptyList()

    @ColumnInfo(name = "minutely")
    private var minutely: List<Minutely> = emptyList()

    constructor()
    constructor(
        timezone: String,
        timezoneOffset: Int,
        lat: Double,
        lon: Double,
        alerts: List<Alert>,
        current: Current,
        daily: List<Daily>,
        hourly: List<Hourly>,
        minutely: List<Minutely>
    ) {
        this.timezone = timezone
        this.timezoneOffset = timezoneOffset
        this.lat = lat
        this.lon = lon
        this.alerts = alerts
        this.current = current
        this.daily = daily
        this.hourly = hourly
        this.minutely = minutely
    }

    fun getTimezone(): String {
        return timezone
    }
    fun setTimezone(timezone: String) {
        this.timezone = timezone
    }

    fun getTimezoneOffset(): Int {
        return timezoneOffset
    }
    fun setTimezoneOffset(timezoneOffset: Int) {
        this.timezoneOffset = timezoneOffset
    }

    fun getLat(): Double {
        return lat
    }
    fun setLat(lat: Double) {
        this.lat = lat
    }

    fun getLon(): Double {
        return lon
    }
    fun setLon(lon: Double) {
        this.lon = lon
    }

    fun getAlerts(): List<Alert> {
        return alerts
    }
    fun setAlerts(alerts: List<Alert>) {
        this.alerts = alerts
    }

    fun getCurrent(): Current {
        return current
    }
    fun setCurrent(current: Current) {
        this.current = current
    }

    fun getDaily(): List<Daily> {
        return daily
    }
    fun setDaily(daily: List<Daily>) {
        this.daily = daily
    }

    fun getHourly(): List<Hourly> {
        return hourly
    }
    fun setHourly(hourly: List<Hourly>) {
        this.hourly = hourly
    }

    fun getMinutely(): List<Minutely> {
        return minutely
    }
    fun setMinutely(minutely: List<Minutely>) {
        this.minutely = minutely
    }
}