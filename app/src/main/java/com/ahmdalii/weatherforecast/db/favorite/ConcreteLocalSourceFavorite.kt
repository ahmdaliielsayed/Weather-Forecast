package com.ahmdalii.weatherforecast.db.favorite

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ahmdalii.weatherforecast.db.AppDataBase
import com.ahmdalii.weatherforecast.model.FavoritePlace

class ConcreteLocalSourceFavorite(context: Context) : LocalSourceFavorite {

    private val dao: FavoriteDAO?
    override val allFavoritePlacesList: LiveData<List<FavoritePlace>>

    init {
        val db: AppDataBase = AppDataBase.getInstance(context)
        dao = db.favoriteDAO()
        allFavoritePlacesList = dao?.favoritePlacesList ?: MutableLiveData<List<FavoritePlace>>().apply { value = emptyList() }
    }

    override fun deleteFavoritePlace(favoritePlace: FavoritePlace) {
        dao?.deleteFavoritePlace(favoritePlace)
    }

    override fun insertFavoritePlace(favoritePlace: FavoritePlace) {
        dao?.insertFavoritePlace(favoritePlace)
    }

    override fun updateFavoritePlace(favoritePlace: FavoritePlace) {
        dao?.updateFavoritePlace(favoritePlace)
    }
}
