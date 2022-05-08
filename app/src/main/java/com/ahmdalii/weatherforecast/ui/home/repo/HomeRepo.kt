package com.ahmdalii.weatherforecast.ui.home.repo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.ahmdalii.weatherforecast.model.WeatherModel
import com.ahmdalii.weatherforecast.network.RemoteSource
import com.ahmdalii.weatherforecast.utils.AppConstants.APPLICATION_LANGUAGE
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_ADMIN_AREA
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_LATITUDE
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_LOCALITY
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_LONGITUDE
import com.ahmdalii.weatherforecast.utils.AppConstants.MEASUREMENT_UNIT
import com.ahmdalii.weatherforecast.utils.AppConstants.NOTIFICATION
import com.ahmdalii.weatherforecast.utils.AppConstants.SETTING_FILE
import com.ahmdalii.weatherforecast.utils.AppSharedPref
import com.google.android.gms.location.*
import retrofit2.Response
import java.io.IOException
import java.util.*

class HomeRepo private constructor(private var remoteSource: RemoteSource): HomeRepoInterface{

    companion object{
        private var instance: HomeRepoInterface? = null
        fun getInstance(remoteSource: RemoteSource): HomeRepoInterface {
            return instance ?: HomeRepo(remoteSource)
        }
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun saveUpdateLocation(context: Context) {
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

    private fun saveLocationData(context: Context, location: Location) {
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(LOCATION_LATITUDE, location.latitude.toFloat())
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(LOCATION_LONGITUDE, location.longitude.toFloat())
    }

    override fun isNotificationChecked(context: Context, isChecked: Boolean) {
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(NOTIFICATION, isChecked)
    }

    override suspend fun getCurrentWeatherOverNetwork(context: Context): Response<WeatherModel> {
        val latitude =
            AppSharedPref.getInstance(context, SETTING_FILE).getFloatValue(LOCATION_LATITUDE)
        val longitude =
            AppSharedPref.getInstance(context, SETTING_FILE).getFloatValue(LOCATION_LONGITUDE)
        val langAttribute: String = if (Locale.getDefault().displayLanguage.equals("العربية")) {
            "ar"
        } else {
            "en"
        }
        val language =
            AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(APPLICATION_LANGUAGE, langAttribute)
        val measurementUnit =
            AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(MEASUREMENT_UNIT, "")
        Log.d("asdfg:repoLat", "$latitude")
        Log.d("asdfg:repoLon", "$longitude")
        Log.d("asdfg:repoLan", langAttribute)
        Log.d("asdfg:repoLang", language)
        Log.d("asdfg:repoUnit", measurementUnit)
        return remoteSource.getCurrentWeatherOverNetwork(latitude, longitude, language, measurementUnit)
    }

    override fun getCurrentLocation(context: Context): List<String> {
        return listOf(
            AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(LOCATION_ADMIN_AREA, ""),
            AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(LOCATION_LOCALITY, "")
        )
    }

    override fun getCurrentTempMeasurementUnit(context: Context): String {
        return AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(MEASUREMENT_UNIT, "")
    }
}