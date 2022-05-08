package com.ahmdalii.weatherforecast.ui.home.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmdalii.weatherforecast.model.WeatherModel
import com.ahmdalii.weatherforecast.ui.home.repo.HomeRepoInterface
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel(private val _repo: HomeRepoInterface) : ViewModel() {

    private var _weatherModelResponse = MutableLiveData<WeatherModel>()
    val weatherModelResponse: LiveData<WeatherModel> = _weatherModelResponse

    private var _errorMsgResponse = MutableLiveData<String>()
    val errorMsgResponse: LiveData<String> = _errorMsgResponse

    private var _currentLocation = MutableLiveData<List<String>>()
    val currentLocation: LiveData<List<String>> = _currentLocation

    private var _currentTempMeasurementUnit = MutableLiveData<String>()
    val currentTempMeasurementUnit: LiveData<String> = _currentTempMeasurementUnit

    private val coroutineExceptionHandler = CoroutineExceptionHandler{ _, t ->
        run {
            t.printStackTrace()
            Log.d("asdfg:coroutine", t.message.toString())
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

    fun getCurrentWeatherOverNetwork(context: Context) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val currentWeatherResponse = _repo.getCurrentWeatherOverNetwork(context)
            if (currentWeatherResponse.isSuccessful) {
                Log.d("asdfg:", "currentWeatherResponse.isSuccessful")
                _weatherModelResponse.postValue(currentWeatherResponse.body())
            } else {
                Log.d("asdfg:", currentWeatherResponse.message())
                _errorMsgResponse.postValue(currentWeatherResponse.message())
            }
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
}