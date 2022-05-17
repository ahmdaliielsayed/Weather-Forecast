package com.ahmdalii.weatherforecast.ui.favorite.view

import com.ahmdalii.weatherforecast.model.FavoritePlace

interface OnFavoriteClickListener {

    fun onRemoveClick(favoritePlace: FavoritePlace)
    fun onPlaceClick(favoritePlace: FavoritePlace)
}