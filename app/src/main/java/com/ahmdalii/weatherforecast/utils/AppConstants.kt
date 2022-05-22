package com.ahmdalii.weatherforecast.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.room.TypeConverter
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.model.MyAlert
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

object AppConstants {

    const val SPLASH_TIME_OUT: Long = 5000
    private const val WIND_SPEED_FACTOR = 2.23693629

    const val SETTING_FILE: String = "setting file"
    const val LOCATION_LONGITUDE: String = "location longitude"
    const val LOCATION_LATITUDE: String = "location latitude"
    const val DEVICE_LONGITUDE: String = "device longitude"
    const val DEVICE_LATITUDE: String = "device latitude"
    const val CURRENT_DEVICE_LOCATION: String = "current device location"
    const val LOCATION_ADMIN_AREA: String = "location admin area"
    const val LOCATION_LOCALITY: String = "location locality"
    const val APPLICATION_LANGUAGE: String = "application language"
    const val APPLICATION_LANGUAGE_AR: String = "ar"
    const val APPLICATION_LANGUAGE_EN: String = "en"
    const val MEASUREMENT_UNIT: String = "measurement unit"
    /*
    * default: kelvin ==> metre/sec
    * metric: Celsius ==> metre/sec
    * imperial: Fahrenheit ==> miles/hour
    * */
    const val MEASUREMENT_UNIT_STANDARD: String = ""
    const val MEASUREMENT_UNIT_METRIC: String = "metric"
    const val MEASUREMENT_UNIT_IMPERIAL: String = "imperial"
    const val NOTIFICATION: String = "notification"
    const val FIRST_TIME_COMPLETED: String = "fist time completed"
    const val CURRENT_TIMEZONE: String = "current timezone"
    const val LOCATION_METHOD_GPS: String = "GPS"
    const val LOCATION_METHOD_MAP: String = "Map"
    const val LOCATION_METHOD: String = "location method"
    const val WIND_SPEED_UNIT: String = "windSpeed unit"
    const val WIND_SPEED_UNIT_M_P_S: String = "meter per second"
    const val WIND_SPEED_UNIT_M_P_H: String = "mile per hour"
    const val COMING_FROM: String = "coming from"
    const val INITIAL_DIALOG: String = "initial dialog"
    const val FAVORITE_FRAGMENT: String = "favorite"
    const val SETTING_FRAGMENT: String = "setting"
    const val REPLY_INTENT_KEY: String = "reply intent"
    const val FAVORITE_KEY: String = "weather intent"

    const val ALARM_CHECKED: String = "alarm checked"
    const val NOTIFICATION_CHECKED: String = "notification checked"
    const val MY_ALERT: String = "my alert"
    const val DESCRIPTION: String = "description"
    const val ICON: String = "icon"
    const val FROM_TIME_IN_MILLIS: String = "fromTimeInMillis"

    lateinit var BASE_URL: String
//    const val IMG_URL: String = BuildConfig.IMG_URL
    lateinit var WEATHER_APP_ID: String

    lateinit var drawerLayout: DrawerLayout
    var atNight: Boolean = false

    fun showAlert(context: Context, title: Int, message: String, icon: Int) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(R.string.ok) { _, _ -> }
            .setIcon(icon)
            .show()
    }

    @SuppressLint("SimpleDateFormat")
    fun getDateTime(dt: Int, pattern: String, language: String): String {
        val format = SimpleDateFormat(pattern, Locale(language))
//        format.timeZone = TimeZone.getTimeZone("GMT+2")
        format.timeZone = TimeZone.getDefault()
        return format.format(Date(dt * 1000L))
    }

    fun getDateTime(milliSeconds: Long, pattern: String, language: String): String {
        val formatter = SimpleDateFormat(pattern, Locale(language))

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = milliSeconds
        return formatter.format(calendar.time)
    }

    fun getDateTime(calendar: Calendar, pattern: String, language: String): String {
        val format = SimpleDateFormat(pattern, Locale(language))
        format.timeZone = TimeZone.getDefault()
        return format.format(calendar.time)
    }

    fun isInternetAvailable(context: Context): Boolean {
        val result: Boolean
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        result = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }

        return result
    }

    fun checkLocationPermissions(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isLocationEnabled(context: Context): Boolean {
        return LocationManagerCompat.isLocationEnabled(context.getSystemService(Context.LOCATION_SERVICE) as LocationManager)
    }

    fun getDisplayCurrentLanguage(): String {
        return if (Locale.getDefault().displayLanguage.equals("العربية")) {
            APPLICATION_LANGUAGE_AR
        } else {
            APPLICATION_LANGUAGE_EN
        }
    }

    fun getGeocoder(context: Context): Geocoder {
        return when (AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(APPLICATION_LANGUAGE, getDisplayCurrentLanguage())) {
            APPLICATION_LANGUAGE_EN -> {
                Geocoder(context, Locale.ENGLISH)
            }
            else -> {
                Geocoder(context, Locale.getDefault())
            }
        }
    }

    fun getPlaceName(context: Context, latitude: Double, longitude: Double): Address {
        val gcd: Geocoder = getGeocoder(context)
        val addresses: List<Address>
        var address = Address(Locale(getDisplayCurrentLanguage()))

        try {
            addresses = gcd.getFromLocation(latitude, longitude, 1)

            if (addresses.isNotEmpty()) {
                address = addresses[0]
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return address
    }

    /*private fun printLogAddress(address: Address) {
        Log.d("lastLocation:", address.locality)
        Log.d("lLoc:getAddressLine", address.getAddressLine(0)) // 5C2P+5R، ديسط، مركز طلخا،، الدقهلية، مصر
        Log.d("lLoc:getLocality", address.locality) // ديسط
        Log.d("lLoc:getCountryName", address.countryName) // مصر
        Log.d("lLoc:getFeatureName", address.featureName) // 5C2P+5R
        Log.d("lLoc:getAdminArea", address.adminArea) // الدقهلية
        Log.d("lLoc:getSubAdminArea", address.subAdminArea) // مركز طلخا،
        Log.d("lLoc:getCountryCode", address.countryCode) // EG
    }*/

    fun playAnimation(view: View, context: Context, animation: Int) {
        view.startAnimation(AnimationUtils.loadAnimation(context, animation))
    }

    fun convertWindSpeedToMPH(windSpeed: Double): Double {
        return windSpeed * WIND_SPEED_FACTOR
    }

    fun convertWindSpeedToMPS(windSpeed: Double): Double {
        return windSpeed.div(WIND_SPEED_FACTOR)
    }

    fun setAppLocale(context: Context, language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getIcon(imageString: String): Int {
        val imageInInteger: Int
        when (imageString) {
            "01d" -> imageInInteger = R.drawable.icon_01d
            "01n" -> imageInInteger = R.drawable.icon_01n
            "02d" -> imageInInteger = R.drawable.icon_02d
            "02n" -> imageInInteger = R.drawable.icon_02n
            "03n" -> imageInInteger = R.drawable.icon_03n
            "03d" -> imageInInteger = R.drawable.icon_03d
            "04d" -> imageInInteger = R.drawable.icon_04d
            "04n" -> imageInInteger = R.drawable.icon_04n
            "09d" -> imageInInteger = R.drawable.icon_09d
            "09n" -> imageInInteger = R.drawable.icon_09n
            "10d" -> imageInInteger = R.drawable.icon_10d
            "10n" -> imageInInteger = R.drawable.icon_10n
            "11d" -> imageInInteger = R.drawable.icon_11d
            "11n" -> imageInInteger = R.drawable.icon_11n
            "13d" -> imageInInteger = R.drawable.icon_13d
            "13n" -> imageInInteger = R.drawable.icon_13n
            "50d" -> imageInInteger = R.drawable.icon_50d
            "50n" -> imageInInteger = R.drawable.icon_50n
            else -> imageInInteger = R.drawable.icon_50n
        }
        return imageInInteger
    }

    fun openNotification(context: Context, myAlert: MyAlert, description: String, icon: String, title: String) {
        val notificationHelper = Notification(context, description, icon, title)
        val nb = notificationHelper.getChannelNotification()
        notificationHelper.getManager()!!.notify(myAlert.id.hashCode(), nb.build())
    }

    @TypeConverter
    fun convertToMyAlert(value: String): MyAlert {
        val type: Type = object : TypeToken<MyAlert>() {}.type
        return Gson().fromJson(value, type)
    }
    @TypeConverter
    fun convertMyAlertToString(myAlert: MyAlert): String = Gson().toJson(myAlert)

    fun showBannerAd(adView: AdView) {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        adView.adListener = object: AdListener() {
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError : LoadAdError) {
                // Code to be executed when an ad request fails.
            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                adView.visibility = View.VISIBLE
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        }
    }
}