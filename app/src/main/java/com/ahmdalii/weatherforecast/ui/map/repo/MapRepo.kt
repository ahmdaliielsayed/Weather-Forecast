package com.ahmdalii.weatherforecast.ui.map.repo

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.db.favorite.LocalSourceFavorite
import com.ahmdalii.weatherforecast.model.FavoritePlace
import com.ahmdalii.weatherforecast.utils.AppConstants.CURRENT_DEVICE_LOCATION
import com.ahmdalii.weatherforecast.utils.AppConstants.DEVICE_LATITUDE
import com.ahmdalii.weatherforecast.utils.AppConstants.DEVICE_LONGITUDE
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_ADMIN_AREA
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_LATITUDE
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_LOCALITY
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_LONGITUDE
import com.ahmdalii.weatherforecast.utils.AppConstants.SETTING_FILE
import com.ahmdalii.weatherforecast.utils.AppConstants.getGeocoder
import com.ahmdalii.weatherforecast.utils.AppConstants.getPlaceName
import com.ahmdalii.weatherforecast.utils.AppSharedPref
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.io.IOException

class MapRepo private constructor(private var localSourceFavorite: LocalSourceFavorite) : MapRepoInterface {

    companion object {
        private var instance: MapRepoInterface? = null
        fun getInstance(localSourceFavorite: LocalSourceFavorite): MapRepoInterface {
            return instance ?: MapRepo(localSourceFavorite)
        }
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun getDeviceLocation(context: Context) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            setupLocation(context)

            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper(),
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
                        val placeName = getPlaceName(context, location.latitude, location.longitude)
                        saveCurrentPlaceName(context, placeName)
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

    private fun saveCurrentPlaceName(context: Context, address: Address) {
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(LOCATION_ADMIN_AREA, address.adminArea ?: context.getString(R.string.unknown_adminArea))
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(LOCATION_LOCALITY, address.locality ?: context.getString(R.string.unknown_locality))
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
        val gcd: Geocoder = getGeocoder(context)
        var addresses: List<Address> = emptyList()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gcd.getFromLocationName(searchForPlace, 1) { addressList -> addresses = addressList }
            } else {
                addresses = gcd.getFromLocationName(searchForPlace, 1) ?: emptyList()
            }

            if (addresses.isNotEmpty()) {
                val location = Location(CURRENT_DEVICE_LOCATION)
                location.longitude = addresses[0].longitude
                location.latitude = addresses[0].latitude
                saveLocationData(context, location)
                saveCurrentPlaceName(context, addresses[0])
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return addresses[0]
    }

    override fun saveUpdateLocationPlace(context: Context, latLng: LatLng, address: Address) {
        val location = Location(CURRENT_DEVICE_LOCATION)
        location.longitude = latLng.longitude
        location.latitude = latLng.latitude
        saveLocationData(context, location)
        saveCurrentPlaceName(context, address)
    }

    override fun insertFavoritePlace(favoritePlace: FavoritePlace) {
        localSourceFavorite.insertFavoritePlace(favoritePlace)
    }

    private fun saveLocationData(context: Context, location: Location) {
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(LOCATION_LATITUDE, location.latitude.toFloat())
        AppSharedPref.getInstance(context, SETTING_FILE).setValue(LOCATION_LONGITUDE, location.longitude.toFloat())
    }
}
