package com.ahmdalii.weatherforecast.db

import androidx.room.TypeConverter
import com.ahmdalii.weatherforecast.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class Converters {

    @TypeConverter
    fun convertToAlertList(value: String): List<Alert>? {
        val type: Type = object : TypeToken<List<Alert>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun convertAlertListToString(list: List<Alert>?): String = Gson().toJson(list)

    @TypeConverter
    fun convertToAlertTagList(value: String): List<String> {
        val type: Type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun convertAlertTagListToString(list: List<String>): String = Gson().toJson(list)

    @TypeConverter
    fun convertToCurrent(value: String): Current {
        val type: Type = object : TypeToken<Current>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun convertCurrentToString(current: Current): String = Gson().toJson(current)

    @TypeConverter
    fun convertToCurrentWeatherList(value: String): List<Weather> {
        val type: Type = object : TypeToken<List<Weather>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun convertCurrentWeatherListToString(list: List<Weather>): String = Gson().toJson(list)

    @TypeConverter
    fun convertToDailyList(value: String): List<Daily> {
        val type: Type = object : TypeToken<List<Daily>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun convertDailyListToString(list: List<Daily>): String = Gson().toJson(list)

    @TypeConverter
    fun convertToFeelsLike(value: String): FeelsLike {
        val type: Type = object : TypeToken<FeelsLike>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun convertFeelsLikeToString(list: FeelsLike): String = Gson().toJson(list)

    @TypeConverter
    fun convertToTemp(value: String): Temp {
        val type: Type = object : TypeToken<Temp>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun convertTempToString(list: Temp): String = Gson().toJson(list)

    @TypeConverter
    fun convertToHourlyList(value: String): List<Hourly> {
        val type: Type = object : TypeToken<List<Hourly>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun convertHourlyListToString(list: List<Hourly>): String = Gson().toJson(list)

    @TypeConverter
    fun convertToMinutelyList(value: String): List<Minutely>? {
        val type: Type = object : TypeToken<List<Minutely>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun convertMinutelyListToString(list: List<Minutely>?): String = Gson().toJson(list)
}
