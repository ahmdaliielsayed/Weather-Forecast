package com.ahmdalii.weatherforecast.db.favorite

import androidx.lifecycle.LiveData
import com.ahmdalii.weatherforecast.model.Alert
import com.ahmdalii.weatherforecast.model.FavoritePlace
import com.ahmdalii.weatherforecast.model.WeatherModel

interface LocalSourceFavorite {

    fun deleteFavoritePlace(favoritePlace: FavoritePlace)
    val allFavoritePlacesList: LiveData<List<FavoritePlace>>
    fun insertFavoritePlace(favoritePlace: FavoritePlace)
    fun updateFavoritePlace(favoritePlace: FavoritePlace)
}