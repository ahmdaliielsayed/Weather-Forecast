package com.ahmdalii.weatherforecast.ui.map.repo

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
import com.ahmdalii.weatherforecast.utils.AppConstants
import com.ahmdalii.weatherforecast.utils.AppConstants.APPLICATION_LANGUAGE
import com.ahmdalii.weatherforecast.utils.AppConstants.APPLICATION_LANGUAGE_EN
import com.ahmdalii.weatherforecast.utils.AppConstants.CURRENT_DEVICE_LOCATION
import com.ahmdalii.weatherforecast.utils.AppConstants.DEVICE_LATITUDE
import com.ahmdalii.weatherforecast.utils.AppConstants.DEVICE_LONGITUDE
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_ADMIN_AREA
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_LATITUDE
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_LOCALITY
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_LONGITUDE
import com.ahmdalii.weatherforecast.utils.AppConstants.SETTING_FILE
import com.ahmdalii.weatherforecast.utils.AppSharedPref
import com.google.android.gms.location.*
import java.io.IOException
import java.util.*


class MapRepo private constructor(/*private var remoteSource: RemoteSource, private var localSource: LocalSource*/): MapRepoInterface{

    companion object{
        private var instance: MapRepoInterface? = null
        fun getInstance(/*remoteSource: RemoteSource, localSource: LocalSource*/): MapRepoInterface {
            return instance ?: MapRepo(/*remoteSource, localSource*/)
        }
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun getDeviceLocation(context: Context) {
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
                        saveDeviceLocationData(context, location)
                        getPlaceName(context, location.latitude, location.longitude)
                        stopLocationUpdates()
                    }
                }
            }
        }
    }

    private fun saveDeviceLocationData(context: Context, location: Location) {
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(DEVICE_LATITUDE, location.latitude.toFloat())
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(DEVICE_LONGITUDE, location.longitude.toFloat())
    }

    private fun getPlaceName(context: Context, latitude: Double, longitude: Double) {
        val gcd: Geocoder = when (AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(APPLICATION_LANGUAGE, "")) {
            APPLICATION_LANGUAGE_EN -> {
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

    override fun getAppSharedPref(context: Context): SharedPreferences {
        return AppSharedPref.getInstance(context, SETTING_FILE).getAppSharedPref()
    }

    override fun isLocationSet(context: Context, key: String): Boolean {
        return AppSharedPref.getInstance(context, SETTING_FILE).getFloatValue(key) != 0f
    }

    override fun getLocationLatitude(context: Context): Float {
        return AppSharedPref.getInstance(context, SETTING_FILE).getFloatValue(DEVICE_LATITUDE)
    }

    override fun getLocationLongitude(context: Context): Float {
        return AppSharedPref.getInstance(context, SETTING_FILE).getFloatValue(DEVICE_LONGITUDE)
    }

    override fun getCurrentDeviceLocation(context: Context): Location {
        val currentLocation = Location(CURRENT_DEVICE_LOCATION)
        currentLocation.latitude = getLocationLatitude(context).toDouble()
        currentLocation.longitude = getLocationLongitude(context).toDouble()

        return currentLocation
    }

    override fun getCurrentAddress(context: Context, searchForPlace: String): Address {
        val gcd: Geocoder = when (AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(APPLICATION_LANGUAGE, "")) {
            APPLICATION_LANGUAGE_EN -> {
                Geocoder(context, Locale.ENGLISH)
            }
            else -> {
                Geocoder(context, Locale.getDefault())
            }
        }
        var addresses: List<Address> = emptyList()
        try {
            addresses = gcd.getFromLocationName(searchForPlace, 1)

            if (addresses.isNotEmpty()) {
                Log.d("lastLocation:", addresses[0].locality)
                val location = Location(CURRENT_DEVICE_LOCATION)
                location.longitude = addresses[0].longitude
                location.latitude = addresses[0].latitude
                saveLocationData(context, location)
                saveCurrentPlaceName(context, addresses)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addresses[0]
    }

    private fun saveLocationData(context: Context, location: Location) {
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(LOCATION_LATITUDE, location.latitude.toFloat())
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(LOCATION_LONGITUDE, location.longitude.toFloat())
    }
}