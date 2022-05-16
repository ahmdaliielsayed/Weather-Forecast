package com.ahmdalii.weatherforecast.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FavoritePlace")
data class FavoritePlace(
    val latitude: Double,
    val longitude: Double,
    val adminArea: String,
    @PrimaryKey
    val locality: String
)
