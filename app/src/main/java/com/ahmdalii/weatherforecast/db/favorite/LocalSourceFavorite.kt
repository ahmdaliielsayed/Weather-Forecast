package com.ahmdalii.weatherforecast.db.favorite

import androidx.lifecycle.LiveData
import com.ahmdalii.weatherforecast.model.Alert
import com.ahmdalii.weatherforecast.model.FavoritePlace
import com.ahmdalii.weatherforecast.model.WeatherModel

interface LocalSourceFavorite {

    fun insertFavoritePlace(favoritePlace: FavoritePlace)
    val allFavoritePlacesList: LiveData<List<FavoritePlace>>
}