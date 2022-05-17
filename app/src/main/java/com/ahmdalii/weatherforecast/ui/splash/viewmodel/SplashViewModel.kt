package com.ahmdalii.weatherforecast.ui.splash.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.model.WeatherModel
import com.ahmdalii.weatherforecast.ui.setting.repo.SettingsRepoInterface
import com.ahmdalii.weatherforecast.ui.splash.repo.SplashRepoInterface
import com.ahmdalii.weatherforecast.utils.AppConstants
import com.ahmdalii.weatherforecast.utils.AppConstants.APPLICATION_LANGUAGE
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_METHOD
import com.ahmdalii.weatherforecast.utils.AppConstants.MEASUREMENT_UNIT
import com.ahmdalii.weatherforecast.utils.AppConstants.NOTIFICATION
import com.ahmdalii.weatherforecast.utils.AppConstants.WIND_SPEED_UNIT
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SplashViewModel(private val _repo: SplashRepoInterface) : ViewModel() {

    private var _language = MutableLiveData<String>()
    val language: LiveData<String> = _language

    private var _errorMsgResponse = MutableLiveData<String>()
    val errorMsgResponse: LiveData<String> = _errorMsgResponse

    private val coroutineExceptionHandler = CoroutineExceptionHandler{ _, t ->
        run {
            t.printStackTrace()
            _errorMsgResponse.postValue(t.message)
        }
    }

    fun getLanguage(context: Context) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _language.postValue(_repo.getLanguage(context))
        }
    }
}