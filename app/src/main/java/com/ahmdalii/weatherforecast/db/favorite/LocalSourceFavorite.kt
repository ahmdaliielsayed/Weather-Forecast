package com.ahmdalii.weatherforecast.db.favorite

import androidx.lifecycle.LiveData
import com.ahmdalii.weatherforecast.model.FavoritePlace

interface LocalSourceFavorite {

    fun deleteFavoritePlace(favoritePlace: FavoritePlace)
    val allFavoritePlacesList: LiveData<List<FavoritePlace>>
    fun insertFavoritePlace(favoritePlace: FavoritePlace)
    fun updateFavoritePlace(favoritePlace: FavoritePlace)
}