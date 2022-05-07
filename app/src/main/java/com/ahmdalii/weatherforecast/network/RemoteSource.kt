package com.ahmdalii.weatherforecast.network

import com.ahmdalii.weatherforecast.model.WeatherModel
import retrofit2.Response

interface RemoteSource {

    suspend fun getCurrentWeatherOverNetwork(latitude: Float, longitude: Float, language: String, measurementUnit: String): Response<WeatherModel>
}