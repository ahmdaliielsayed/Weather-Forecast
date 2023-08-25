package com.ahmdalii.weatherforecast.ui.notification.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ahmdalii.weatherforecast.ui.notification.repo.NotificationRepoInterface

class NotificationViewModelFactory(private val _repo: NotificationRepoInterface) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            NotificationViewModel(_repo) as T
        } else {
            throw IllegalArgumentException("NotificationViewModel Class not found")
        }
    }
}
