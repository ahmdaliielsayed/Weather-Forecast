package com.ahmdalii.weatherforecast.ui.favorite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ahmdalii.weatherforecast.ui.favorite.repo.FavoriteRepoInterface

class FavoriteViewModelFactory(private val _repo: FavoriteRepoInterface) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(FavoriteViewModel::class.java)) {
            FavoriteViewModel(_repo) as T
        } else {
            throw IllegalArgumentException("FavoriteViewModel Class not found")
        }
    }
}
