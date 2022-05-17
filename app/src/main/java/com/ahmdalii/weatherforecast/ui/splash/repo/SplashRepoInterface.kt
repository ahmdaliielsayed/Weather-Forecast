package com.ahmdalii.weatherforecast.ui.splash.repo

import android.content.Context
import android.content.SharedPreferences

interface SplashRepoInterface {
    fun getLanguage(context: Context): String
}