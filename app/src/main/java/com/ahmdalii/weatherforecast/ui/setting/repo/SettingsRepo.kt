package com.ahmdalii.weatherforecast.ui.setting.repo

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
import com.ahmdalii.weatherforecast.utils.AppConstants.APPLICATION_LANGUAGE
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
import com.ahmdalii.weatherforecast.utils.AppSharedPref
import com.google.android.gms.location.*
import java.io.IOException
import java.util.*

class SettingsRepo private constructor(): SettingsRepoInterface {

    companion object{
        private var instance: SettingsRepoInterface? = null
        fun getInstance(): SettingsRepoInterface {
            return instance ?: SettingsRepo()
        }
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun setLocationMethod(context: Context, locationMethod: String) {
        /*
        * LOCATION_METHOD_GPS
        * LOCATION_METHOD_MAP
        * */
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(LOCATION_METHOD, locationMethod)
        if (locationMethod == LOCATION_METHOD_GPS) {
            saveUpdateLocation(context)
        } else {
            // work with map !!!
        }
    }

    override fun getLocationMethod(context: Context): String {
        return AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(LOCATION_METHOD, LOCATION_METHOD_GPS)
    }

    override fun setLanguage(context: Context, language: String) {
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(APPLICATION_LANGUAGE, language)
        // work with new language over all of the application
    }

    override fun getLanguage(context: Context): String {
        val langAttribute: String = if (Locale.getDefault().displayLanguage.equals("العربية")) {
            "ar"
        } else {
            "en"
        }
        return AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(APPLICATION_LANGUAGE, langAttribute)
    }

    override fun setCurrentTempMeasurementUnit(context: Context, measurementUnit: String) {
        /*
        * MEASUREMENT_UNIT_STANDARD
        * MEASUREMENT_UNIT_METRIC
        * MEASUREMENT_UNIT_IMPERIAL
        * */
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(MEASUREMENT_UNIT, measurementUnit)
    }

    override fun getCurrentTempMeasurementUnit(context: Context): String {
        return AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(MEASUREMENT_UNIT, MEASUREMENT_UNIT_STANDARD)
    }

    override fun setWindSpeedUnit(context: Context, windSpeedUnit: String) {
        /*
        * context.getString(R.string.m_p_s) ==> WIND_SPEED_UNIT_M_P_S
        * context.getString(R.string.m_p_h) ==> WIND_SPEED_UNIT_M_P_H
        * */
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(WIND_SPEED_UNIT, windSpeedUnit)
    }

    override fun getWindSpeedUnit(context: Context): String {
        return AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(WIND_SPEED_UNIT, WIND_SPEED_UNIT_M_P_S)
    }

    override fun setNotificationChecked(context: Context, isChecked: Boolean) {
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(NOTIFICATION, isChecked)
    }

    override fun getNotificationChecked(context: Context): Boolean {
        return AppSharedPref.getInstance(context, SETTING_FILE).getBooleanValue(NOTIFICATION, true)
    }

    override fun getAppSharedPref(context: Context): SharedPreferences {
        return AppSharedPref.getInstance(context, SETTING_FILE).getAppSharedPref()
    }

    private fun saveUpdateLocation(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setupLocation(context)

            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun setupLocation(context: Context) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        setupLocationRequest()
        setupLocationCallback(context)
    }

    private fun setupLocationRequest() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun setupLocationCallback(context: Context) {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for (location in locationResult.locations) {
                    if (location != null) {
                        saveLocationData(context, location)
                        getPlaceName(context, location.latitude, location.longitude)
                        stopLocationUpdates()
                    }
                }
            }
        }
    }

    private fun saveLocationData(context: Context, location: Location) {
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(LOCATION_LATITUDE, location.latitude.toFloat())
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(LOCATION_LONGITUDE, location.longitude.toFloat())
    }

    private fun getPlaceName(context: Context, latitude: Double, longitude: Double) {
        val gcd: Geocoder = when (AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(APPLICATION_LANGUAGE, "")) {
            "English" -> {
                Geocoder(context, Locale.ENGLISH)
            }
            else -> {
                Geocoder(context, Locale.getDefault())
            }
        }
        val addresses: List<Address>
        try {
            addresses = gcd.getFromLocation(latitude, longitude, 1)

            if (addresses.isNotEmpty())
                Log.d("lastLocation:", addresses[0].locality)

            Log.d("lLoc:getAddressLine", addresses[0].getAddressLine(0)) // 5C2P+5R، ديسط، مركز طلخا،، الدقهلية، مصر
            Log.d("lLoc:getLocality", addresses[0].locality) // ديسط
            Log.d("lLoc:getCountryName", addresses[0].countryName) // مصر
            Log.d("lLoc:getFeatureName", addresses[0].featureName) // 5C2P+5R
            Log.d("lLoc:getAdminArea", addresses[0].adminArea) // الدقهلية
            Log.d("lLoc:getSubAdminArea", addresses[0].subAdminArea) // مركز طلخا،
            Log.d("lLoc:getCountryCode", addresses[0].countryCode) // EG
            saveCurrentPlaceName(context, addresses)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun saveCurrentPlaceName(context: Context, addresses: List<Address>) {
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(LOCATION_ADMIN_AREA, addresses[0].adminArea)
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(LOCATION_LOCALITY, addresses[0].locality)
    }

    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}