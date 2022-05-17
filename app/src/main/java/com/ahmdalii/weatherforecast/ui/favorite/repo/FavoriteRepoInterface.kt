package com.ahmdalii.weatherforecast.ui.favorite.repo

import android.content.Context
import android.content.SharedPreferences
import android.location.Address
import android.location.Location
import androidx.lifecycle.LiveData
import com.ahmdalii.weatherforecast.model.FavoritePlace
import com.ahmdalii.weatherforecast.model.WeatherModel
import com.google.android.gms.maps.model.LatLng
import retrofit2.Response

interface FavoriteRepoInterface {

    val allFavoritePlaces: LiveData<List<FavoritePlace>>
    fun deleteFavoritePlace(favoritePlace: FavoritePlace)
    fun insertFavoritePlace(favoritePlace: FavoritePlace)
    fun updateCurrentPlaceName(favoritePlace: FavoritePlace)

    suspend fun getCurrentWeatherOverNetwork(context: Context, favoritePlace: FavoritePlace): Response<WeatherModel>

    fun getCurrentTempMeasurementUnit(context: Context): String
    fun getWindSpeedMeasurementUnit(context: Context): String

    fun getLanguage(context: Context): String
}