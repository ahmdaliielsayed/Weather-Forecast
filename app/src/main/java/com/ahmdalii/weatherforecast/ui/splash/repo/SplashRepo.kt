package com.ahmdalii.weatherforecast.ui.splash.repo

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.utils.AppConstants.APPLICATION_LANGUAGE
import com.ahmdalii.weatherforecast.utils.AppConstants.APPLICATION_LANGUAGE_AR
import com.ahmdalii.weatherforecast.utils.AppConstants.APPLICATION_LANGUAGE_EN
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_ADMIN_AREA
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_LATITUDE
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_LOCALITY
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_LONGITUDE
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_METHOD
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_METHOD_GPS
import com.ahmdalii.weatherforecast.utils.AppConstants.MEASUREMENT_UNIT
import com.ahmdalii.weatherforecast.utils.AppConstants.MEASUREMENT_UNIT_STANDARD
import com.ahmdalii.weatherforecast.utils.AppConstants.NOTIFICATION
import com.ahmdalii.weatherforecast.utils.AppConstants.SETTING_FILE
import com.ahmdalii.weatherforecast.utils.AppConstants.WIND_SPEED_UNIT
import com.ahmdalii.weatherforecast.utils.AppConstants.WIND_SPEED_UNIT_M_P_S
import com.ahmdalii.weatherforecast.utils.AppConstants.getDisplayCurrentLanguage
import com.ahmdalii.weatherforecast.utils.AppConstants.getGeocoder
import com.ahmdalii.weatherforecast.utils.AppSharedPref
import com.google.android.gms.location.*
import java.io.IOException
import java.util.*

class SplashRepo private constructor(): SplashRepoInterface {

    companion object{
        private var instance: SplashRepoInterface? = null
        fun getInstance(): SplashRepoInterface {
            return instance ?: SplashRepo()
        }
    }

    override fun getLanguage(context: Context): String {
        return AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(APPLICATION_LANGUAGE, getDisplayCurrentLanguage())
    }
}