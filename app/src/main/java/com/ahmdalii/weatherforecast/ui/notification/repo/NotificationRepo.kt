package com.ahmdalii.weatherforecast.ui.notification.repo

import android.content.Context
import androidx.lifecycle.LiveData
import com.ahmdalii.weatherforecast.db.notification.LocalSourceNotification
import com.ahmdalii.weatherforecast.db.weather.LocalSource
import com.ahmdalii.weatherforecast.model.MyAlert
import com.ahmdalii.weatherforecast.model.WeatherModel
import com.ahmdalii.weatherforecast.utils.AppConstants.APPLICATION_LANGUAGE
import com.ahmdalii.weatherforecast.utils.AppConstants.CURRENT_TIMEZONE
import com.ahmdalii.weatherforecast.utils.AppConstants.NOTIFICATION
import com.ahmdalii.weatherforecast.utils.AppConstants.SETTING_FILE
import com.ahmdalii.weatherforecast.utils.AppConstants.getDisplayCurrentLanguage
import com.ahmdalii.weatherforecast.utils.AppSharedPref

class NotificationRepo private constructor(
    private var localSourceNotification: LocalSourceNotification,
    private var localSource: LocalSource,
) :
    NotificationRepoInterface {

    companion object {
        private var instance: NotificationRepoInterface? = null
        fun getInstance(localSourceNotification: LocalSourceNotification, localSource: LocalSource): NotificationRepoInterface {
            return instance ?: NotificationRepo(localSourceNotification, localSource)
        }
    }

    override val alertList: LiveData<List<MyAlert>>
        get() = localSourceNotification.alertList

    override fun insertAlert(alert: MyAlert): Long {
        return localSourceNotification.insertAlert(alert)
    }

    override fun deleteAlert(alert: MyAlert) {
        localSourceNotification.deleteAlert(alert)
    }

    override fun deleteAlert(id: Long) {
        localSourceNotification.deleteAlert(id)
    }

    override fun getAlert(id: Long): MyAlert {
        return localSourceNotification.getAlert(id)
    }

    override fun getLanguage(context: Context): String {
        return AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(
            APPLICATION_LANGUAGE,
            getDisplayCurrentLanguage(),
        )
    }

    override fun selectAllStoredWeatherModel(context: Context): LiveData<WeatherModel> {
        return localSource.selectAllStoredWeatherModel(getCurrentTimeZone(context))
    }

    private fun getCurrentTimeZone(context: Context): String {
        return AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(CURRENT_TIMEZONE, "")
    }

    override fun getNotificationChecked(context: Context): Boolean {
        return AppSharedPref.getInstance(context, SETTING_FILE).getBooleanValue(NOTIFICATION, true)
    }
}
