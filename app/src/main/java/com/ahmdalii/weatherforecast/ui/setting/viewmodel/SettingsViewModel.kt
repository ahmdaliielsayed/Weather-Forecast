package com.ahmdalii.weatherforecast.ui.setting.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.model.WeatherModel
import com.ahmdalii.weatherforecast.ui.setting.repo.SettingsRepoInterface
import com.ahmdalii.weatherforecast.utils.AppConstants
import com.ahmdalii.weatherforecast.utils.AppConstants.APPLICATION_LANGUAGE
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_METHOD
import com.ahmdalii.weatherforecast.utils.AppConstants.MEASUREMENT_UNIT
import com.ahmdalii.weatherforecast.utils.AppConstants.NOTIFICATION
import com.ahmdalii.weatherforecast.utils.AppConstants.WIND_SPEED_UNIT
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel(private val _repo: SettingsRepoInterface) : ViewModel() {

    private var _locationMethod = MutableLiveData<String>()
    val locationMethod: LiveData<String> = _locationMethod

    private var _language = MutableLiveData<String>()
    val language: LiveData<String> = _language

    private var _currentTempMeasurementUnit = MutableLiveData<String>()
    val currentTempMeasurementUnit: LiveData<String> = _currentTempMeasurementUnit

    private var _windSpeedUnit = MutableLiveData<String>()
    val windSpeedUnit: LiveData<String> = _windSpeedUnit

    private var _notificationChecked = MutableLiveData<Boolean>()
    val notificationChecked: LiveData<Boolean> = _notificationChecked

    private lateinit var preferences: SharedPreferences
    private lateinit var listener: SharedPreferences.OnSharedPreferenceChangeListener

    private var _errorMsgResponse = MutableLiveData<String>()
    val errorMsgResponse: LiveData<String> = _errorMsgResponse

    private var _changedSuccessfully = MutableLiveData<String>()
    val changedSuccessfully: LiveData<String> = _changedSuccessfully

    private val coroutineExceptionHandler = CoroutineExceptionHandler{ _, t ->
        run {
            t.printStackTrace()
            _errorMsgResponse.postValue(t.message)
        }
    }

    fun setLocationMethod(context: Context, locationMethod: String) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _repo.setLocationMethod(context, locationMethod)
        }
    }
    fun getLocationMethod(context: Context) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _locationMethod.postValue(_repo.getLocationMethod(context))
        }
    }

    fun setLanguage(context: Context, language: String) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _repo.setLanguage(context, language)
        }
    }
    fun getLanguage(context: Context) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _language.postValue(_repo.getLanguage(context))
        }
    }

    fun setCurrentTempMeasurementUnit(context: Context, measurementUnit: String) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _repo.setCurrentTempMeasurementUnit(context, measurementUnit)
        }
    }
    fun getCurrentTempMeasurementUnit(context: Context) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _currentTempMeasurementUnit.postValue(_repo.getCurrentTempMeasurementUnit(context))
        }
    }

    fun setWindSpeedUnit(context: Context, windSpeedUnit: String) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _repo.setWindSpeedUnit(context, windSpeedUnit)
        }
    }
    fun getWindSpeedUnit(context: Context) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _windSpeedUnit.postValue(_repo.getWindSpeedUnit(context))
        }
    }

    fun setNotificationChecked(context: Context, isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _repo.setNotificationChecked(context, isChecked)
        }
    }
    fun getNotificationChecked(context: Context) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _notificationChecked.postValue(_repo.getNotificationChecked(context))
        }
    }

    fun observeOnSharedPref(context: Context) {
        preferences = _repo.getAppSharedPref(context)

        listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    LOCATION_METHOD -> {
                        _changedSuccessfully.postValue(context.getString(R.string.changed_successfully))
                    }
                    APPLICATION_LANGUAGE -> {
                        _changedSuccessfully.postValue(context.getString(R.string.changed_successfully))
                    }
                    MEASUREMENT_UNIT -> {
                        _changedSuccessfully.postValue(context.getString(R.string.changed_successfully))
                    }
                    WIND_SPEED_UNIT -> {
                        _changedSuccessfully.postValue(context.getString(R.string.changed_successfully))
                    }
                    NOTIFICATION -> {
                        _changedSuccessfully.postValue(context.getString(R.string.changed_successfully))
                    }
                }
            }
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unRegisterOnSharedPreferenceChangeListener(){
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
}