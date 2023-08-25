package com.ahmdalii.weatherforecast.ui.notification.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmdalii.weatherforecast.model.MyAlert
import com.ahmdalii.weatherforecast.model.WeatherModel
import com.ahmdalii.weatherforecast.ui.notification.repo.NotificationRepoInterface
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationViewModel(private val _repo: NotificationRepoInterface) : ViewModel() {

    private var _errorMsgResponse = MutableLiveData<String>()
    val errorMsgResponse: LiveData<String> = _errorMsgResponse

    private var _myAlert = MutableLiveData<MyAlert>()
    val myAlert: LiveData<MyAlert> = _myAlert

    private var _id = MutableLiveData<Long>()
    val id: LiveData<Long> = _id

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, t ->
        run {
            t.printStackTrace()
            _errorMsgResponse.postValue(t.message)
        }
    }

    fun getAlertList(): LiveData<List<MyAlert>> {
        return _repo.alertList
    }

    fun insertAlert(alert: MyAlert) {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            _id.postValue(_repo.insertAlert(alert))
        }
    }

    fun deleteAlert(alert: MyAlert) {
        viewModelScope.launch(Dispatchers.IO) {
            _repo.deleteAlert(alert)
        }
    }

    fun getAlert(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _myAlert.postValue(_repo.getAlert(id))
        }
    }

    fun getLanguage(context: Context): String {
        return _repo.getLanguage(context)
    }

    fun getAllStoredWeatherModel(context: Context): LiveData<WeatherModel> {
        return _repo.selectAllStoredWeatherModel(context)
    }
}
