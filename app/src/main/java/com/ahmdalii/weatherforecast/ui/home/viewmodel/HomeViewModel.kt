package com.ahmdalii.weatherforecast.ui.home.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmdalii.weatherforecast.model.Hourly
import com.ahmdalii.weatherforecast.model.WeatherModel
import com.ahmdalii.weatherforecast.ui.home.repo.HomeRepoInterface
import com.ahmdalii.weatherforecast.utils.AppConstants
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_LOCALITY
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_LONGITUDE
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val _repo: HomeRepoInterface) : ViewModel() {

    private var _weatherModelResponse = MutableLiveData<WeatherModel>()
    val weatherModelResponse: LiveData<WeatherModel> = _weatherModelResponse

    private var _showProgressBar = MutableLiveData<Boolean>()
    val showProgressBar: LiveData<Boolean> = _showProgressBar

    private var _errorMsgResponse = MutableLiveData<String>()
    val errorMsgResponse: LiveData<String> = _errorMsgResponse

    private var _currentLocation = MutableLiveData<List<String>>()
    val currentLocation: LiveData<List<String>> = _currentLocation

    private var _currentTempMeasurementUnit = MutableLiveData<String>()
    val currentTempMeasurementUnit: LiveData<String> = _currentTempMeasurementUnit

    private var _windSpeedMeasurementUnit = MutableLiveData<String>()
    val windSpeedMeasurementUnit: LiveData<String> = _windSpeedMeasurementUnit

    private lateinit var preferences: SharedPreferences
    private lateinit var listener: SharedPreferences.OnSharedPreferenceChangeListener

    private val coroutineExceptionHandler = CoroutineExceptionHandler{ _, t ->
        run {
            t.printStackTrace()
            Log.e("asdfg:coroutine", t.message.toString())
            _errorMsgResponse.postValue(t.message)
        }
    }

    fun saveUpdateLocation(context: Context) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _repo.saveUpdateLocation(context)
        }
    }

    fun isNotificationChecked(context: Context, isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _repo.isNotificationChecked(context, isChecked)
        }
    }

    private fun getCurrentWeatherOverNetwork(context: Context) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _showProgressBar.postValue(true)
            val currentWeatherResponse = _repo.getCurrentWeatherOverNetwork(context)
            if (currentWeatherResponse.isSuccessful) {
                val hourlyList = mutableListOf<Hourly>()
                for (item in currentWeatherResponse.body()?.hourly!!) {
                    if (hourlyList.size != 24) {
                        hourlyList.add(item)
                    }
                }
                currentWeatherResponse.body()!!.hourly = hourlyList
                saveCurrentWeatherModelToRoom(currentWeatherResponse.body()!!)
                _weatherModelResponse.postValue(currentWeatherResponse.body())
            } else {
                Log.e("asdfg:getCurrWeth", currentWeatherResponse.message().toString())
                _errorMsgResponse.postValue(currentWeatherResponse.message())
            }
        }
    }

    private fun saveCurrentWeatherModelToRoom(weatherModel: WeatherModel) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _repo.insertWeatherModel(weatherModel)
        }
    }

    fun getCurrentLocation(context: Context) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val currentLocationList = _repo.getCurrentLocation(context)
            if (currentLocationList.isNullOrEmpty()) {
                _errorMsgResponse.postValue("${currentLocationList.size} \nerror viewModel getCurrentLocation")
            } else {
                _currentLocation.postValue(currentLocationList)
            }
        }
    }

    fun getCurrentTempMeasurementUnit(context: Context) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _currentTempMeasurementUnit.postValue(_repo.getCurrentTempMeasurementUnit(context))
        }
    }

    fun getWindSpeedMeasurementUnit(context: Context) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _windSpeedMeasurementUnit.postValue(_repo.getWindSpeedMeasurementUnit(context))
        }
    }

    fun observeOnSharedPref(context: Context){
        preferences = _repo.getAppSharedPref(context)
        if (_repo.isLocationSet(context)) {
            getCurrentWeatherOverNetwork(context)
        } else{
            listener =
                SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == LOCATION_LONGITUDE) {
                        if (_repo.isLocationSet(context)) {
                            getCurrentWeatherOverNetwork(context)
                        }
                    } else if (key == LOCATION_LOCALITY) {
                        val currentLocation = _repo.getCurrentLocation(context)
                        if (currentLocation.isNullOrEmpty()) {
                            _errorMsgResponse.postValue("${currentLocation.size} \nerror viewModel getCurrentLocation")
                        } else {
                            _currentLocation.postValue(currentLocation)
                        }
                    }
                }
            preferences.registerOnSharedPreferenceChangeListener(listener)
        }
    }

    fun firstTimeCompleted(context: Context) {
        _repo.firstTimeCompleted(context)
    }
    fun isFirstTimeCompleted(context: Context): Boolean {
        return _repo.isFirstTimeCompleted(context)
    }

    fun getAllStoredWeatherModel(context: Context): LiveData<WeatherModel> {
        return _repo.selectAllStoredWeatherModel(context)
    }

    fun saveLocationMethod(context: Context, locationMethod: String) {
        _repo.setLocationMethod(context, locationMethod)
    }

    fun getLanguage(context: Context): String {
        return _repo.getLanguage(context)
    }
}