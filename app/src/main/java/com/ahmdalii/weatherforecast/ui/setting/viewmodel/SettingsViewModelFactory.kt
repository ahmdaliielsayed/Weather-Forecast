package com.ahmdalii.weatherforecast.ui.setting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ahmdalii.weatherforecast.ui.setting.repo.SettingsRepoInterface

class SettingsViewModelFactory (private val _repo: SettingsRepoInterface): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            SettingsViewModel(_repo) as T
        } else {
            throw IllegalArgumentException("SettingsViewModel Class not found")
        }
    }
}