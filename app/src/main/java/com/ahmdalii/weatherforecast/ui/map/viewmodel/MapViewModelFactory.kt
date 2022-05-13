package com.ahmdalii.weatherforecast.ui.map.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ahmdalii.weatherforecast.ui.map.repo.MapRepoInterface

class MapViewModelFactory (private val _repo: MapRepoInterface): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            MapViewModel(_repo) as T
        } else {
            throw IllegalArgumentException("MapViewModel Class not found")
        }
    }
}