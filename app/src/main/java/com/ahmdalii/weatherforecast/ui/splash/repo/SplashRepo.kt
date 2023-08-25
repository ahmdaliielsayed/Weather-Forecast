package com.ahmdalii.weatherforecast.ui.splash.repo

import android.content.Context
import com.ahmdalii.weatherforecast.utils.AppConstants.APPLICATION_LANGUAGE
import com.ahmdalii.weatherforecast.utils.AppConstants.SETTING_FILE
import com.ahmdalii.weatherforecast.utils.AppConstants.getDisplayCurrentLanguage
import com.ahmdalii.weatherforecast.utils.AppSharedPref

class SplashRepo private constructor() : SplashRepoInterface {

    companion object {
        private var instance: SplashRepoInterface? = null
        fun getInstance(): SplashRepoInterface {
            return instance ?: SplashRepo()
        }
    }

    override fun getLanguage(context: Context): String {
        return AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(APPLICATION_LANGUAGE, getDisplayCurrentLanguage())
    }
}
