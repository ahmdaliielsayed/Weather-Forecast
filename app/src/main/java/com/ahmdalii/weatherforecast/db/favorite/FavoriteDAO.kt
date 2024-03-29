package com.ahmdalii.weatherforecast.db.favorite

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ahmdalii.weatherforecast.model.FavoritePlace

@Dao
interface FavoriteDAO {

    @get:Query("SELECT * FROM FavoritePlace")
    val favoritePlacesList: LiveData<List<FavoritePlace>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavoritePlace(favoritePlace: FavoritePlace)

    @Delete
    fun deleteFavoritePlace(favoritePlace: FavoritePlace)

    @Update
    fun updateFavoritePlace(favoritePlace: FavoritePlace)
}
