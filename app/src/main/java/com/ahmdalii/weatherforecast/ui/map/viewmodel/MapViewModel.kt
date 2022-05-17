package com.ahmdalii.weatherforecast.ui.map.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.location.Address
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.model.FavoritePlace
import com.ahmdalii.weatherforecast.ui.map.repo.MapRepoInterface
import com.ahmdalii.weatherforecast.ui.map.view.MapsActivity
import com.ahmdalii.weatherforecast.utils.AppConstants
import com.ahmdalii.weatherforecast.utils.AppConstants.CURRENT_DEVICE_LOCATION
import com.ahmdalii.weatherforecast.utils.AppConstants.DEVICE_LONGITUDE
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_LONGITUDE
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapViewModel(private val _repo: MapRepoInterface) : ViewModel() {

    private var _errorMsgResponse = MutableLiveData<String>()
    val errorMsgResponse: LiveData<String> = _errorMsgResponse

    private var _currentDeviceLocation = MutableLiveData<Location>()
    val currentDeviceLocation: LiveData<Location> = _currentDeviceLocation

    private var _searchAddress = MutableLiveData<Address>()
    val searchAddress: LiveData<Address> = _searchAddress

    private lateinit var preferences: SharedPreferences
    private lateinit var listener: SharedPreferences.OnSharedPreferenceChangeListener

    private val coroutineExceptionHandler = CoroutineExceptionHandler{ _, t ->
        run {
            t.printStackTrace()
            _errorMsgResponse.postValue(t.message)
        }
    }

    fun getDeviceLocation(context: Context) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _repo.getDeviceLocation(context)
        }
    }

    fun getCurrentAddress(context: Context, searchForPlace: String) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _searchAddress.postValue(_repo.getCurrentAddress(context, searchForPlace))
        }
    }

    fun observeOnSharedPref(context: Context){
        preferences = _repo.getAppSharedPref(context)
        listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
               when (key) {
                    DEVICE_LONGITUDE -> {
                        if (_repo.isLocationSet(context, DEVICE_LONGITUDE)) {
                            val currentLocation = _repo.getCurrentDeviceLocation(context)
                            _currentDeviceLocation.postValue(currentLocation)
                        }
                    }
                }
            }
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unRegisterOnSharedPreferenceChangeListener(){
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun saveUpdateLocationPlace(context: Context, latLng: LatLng, address: Address) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _repo.saveUpdateLocationPlace(context, latLng, address)
        }
    }
}