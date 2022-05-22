package com.ahmdalii.weatherforecast

import android.app.Application
import com.ahmdalii.weatherforecast.utils.AppConstants.BASE_URL
import com.ahmdalii.weatherforecast.utils.AppConstants.WEATHER_APP_ID

class BaseApplication: Application() {

    companion object {
        // Used to load the 'weatherforecast' library on application startup.
        init {
            System.loadLibrary("weatherforecast")
        }
    }

    override fun onCreate() {
        super.onCreate()

        BASE_URL = getBaseURL()
        WEATHER_APP_ID = getWeatherAppID()
    }

    /**
     * A native method that is implemented by the 'myapplication' native library,
     * which is packaged with this application.
     */
    private external fun getBaseURL(): String
    private external fun getWeatherAppID(): String
}