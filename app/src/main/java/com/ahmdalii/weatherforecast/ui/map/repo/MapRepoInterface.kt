package com.ahmdalii.weatherforecast.ui.map.repo

import android.content.Context
import android.content.SharedPreferences
import android.location.Address
import android.location.Location
import com.ahmdalii.weatherforecast.model.FavoritePlace
import com.google.android.gms.maps.model.LatLng

interface MapRepoInterface {

    fun getDeviceLocation(context: Context)

    fun getAppSharedPref(context: Context): SharedPreferences
    fun isLocationSet(context: Context, key: String): Boolean
    fun getLocationLatitude(context: Context): Float
    fun getLocationLongitude(context: Context): Float
    fun getCurrentDeviceLocation(context: Context): Location
    fun getCurrentAddress(context: Context, searchForPlace: String): Address
    fun saveUpdateLocationPlace(context: Context, latLng: LatLng, address: Address)

    fun insertFavoritePlace(favoritePlace: FavoritePlace)
}
