package com.ahmdalii.weatherforecast.ui.splash.repo

import android.content.Context

interface SplashRepoInterface {
    fun getLanguage(context: Context): String
}