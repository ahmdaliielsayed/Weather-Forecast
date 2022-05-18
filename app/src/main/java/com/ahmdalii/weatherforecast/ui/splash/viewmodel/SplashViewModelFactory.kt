package com.ahmdalii.weatherforecast.ui.splash.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ahmdalii.weatherforecast.ui.splash.repo.SplashRepoInterface

class SplashViewModelFactory (private val _repo: SplashRepoInterface): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            SplashViewModel(_repo) as T
        } else {
            throw IllegalArgumentException("SplashViewModel Class not found")
        }
    }
}