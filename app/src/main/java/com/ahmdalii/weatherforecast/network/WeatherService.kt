package com.ahmdalii.weatherforecast.network

import com.ahmdalii.weatherforecast.model.WeatherModel
import com.ahmdalii.weatherforecast.utils.AppConstants
import com.ahmdalii.weatherforecast.utils.AppConstants.BASE_URL
import com.ahmdalii.weatherforecast.utils.AppConstants.WEATHER_APP_ID
import kotlinx.coroutines.Deferred
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    @GET("onecall")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Float,
        @Query("lon") longitude: Float,
        @Query("lang") language: String,
        @Query("units") measurementUnit: String
    ): Response<WeatherModel>
}