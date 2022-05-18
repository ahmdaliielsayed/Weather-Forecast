package com.ahmdalii.weatherforecast.ui.favorite.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmdalii.weatherforecast.model.FavoritePlace
import com.ahmdalii.weatherforecast.model.Hourly
import com.ahmdalii.weatherforecast.model.WeatherModel
import com.ahmdalii.weatherforecast.ui.favorite.repo.FavoriteRepoInterface
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoriteViewModel(private val _repo: FavoriteRepoInterface) : ViewModel() {

    private var _weatherModelResponse = MutableLiveData<WeatherModel>()
    val weatherModelResponse: LiveData<WeatherModel> = _weatherModelResponse

    private var _animationView = MutableLiveData<Boolean>()
    val animationView: LiveData<Boolean> = _animationView

    private var _currentTempMeasurementUnit = MutableLiveData<String>()
    val currentTempMeasurementUnit: LiveData<String> = _currentTempMeasurementUnit

    private var _windSpeedMeasurementUnit = MutableLiveData<String>()
    val windSpeedMeasurementUnit: LiveData<String> = _windSpeedMeasurementUnit

    private var _errorMsgResponse = MutableLiveData<String>()
    val errorMsgResponse: LiveData<String> = _errorMsgResponse

    private val coroutineExceptionHandler = CoroutineExceptionHandler{ _, t ->
        run {
            t.printStackTrace()
            _errorMsgResponse.postValue(t.message)
        }
    }

    fun getFavoritePlacesList(): LiveData<List<FavoritePlace>> {
        return _repo.allFavoritePlaces
    }

    fun deleteFavoritePlace(favoritePlace: FavoritePlace) {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.deleteFavoritePlace(favoritePlace)
        }
    }

    fun insertFavoritePlace(favoritePlace: FavoritePlace) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _repo.insertFavoritePlace(favoritePlace)
        }
    }

    fun getCurrentWeatherOverNetwork(context: Context, favoritePlace: FavoritePlace) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _animationView.postValue(true)
            val currentWeatherResponse = _repo.getCurrentWeatherOverNetwork(context, favoritePlace)
            if (currentWeatherResponse.isSuccessful) {
                val hourlyList = mutableListOf<Hourly>()
                for (item in currentWeatherResponse.body()?.hourly!!) {
                    if (hourlyList.size != 24) {
                        hourlyList.add(item)
                    }
                }
                currentWeatherResponse.body()!!.hourly = hourlyList
                _weatherModelResponse.postValue(currentWeatherResponse.body())
            } else {
                _errorMsgResponse.postValue(currentWeatherResponse.message())
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

    fun getLanguage(context: Context): String {
        return _repo.getLanguage(context)
    }

    fun updateCurrentPlaceName(favoritePlace: FavoritePlace) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _repo.updateCurrentPlaceName(favoritePlace)
        }
    }
}