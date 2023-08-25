package com.ahmdalii.weatherforecast.utils

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.ahmdalii.weatherforecast.model.MyAlert
import com.ahmdalii.weatherforecast.utils.AppConstants.DESCRIPTION
import com.ahmdalii.weatherforecast.utils.AppConstants.FROM_TIME_IN_MILLIS
import com.ahmdalii.weatherforecast.utils.AppConstants.ICON
import com.ahmdalii.weatherforecast.utils.AppConstants.MY_ALERT
import com.ahmdalii.weatherforecast.utils.AppConstants.convertMyAlertToString
import java.util.*
import java.util.concurrent.TimeUnit

object WorkRequestManager {

    fun createWorkRequest(
        alert: MyAlert,
        description: String,
        icon: String,
        context: Context,
        fromTimeInMillis: Long,
    ) {
        val data = Data.Builder()
            .putString(MY_ALERT, convertMyAlertToString(alert))
            .putString(DESCRIPTION, description)
            .putString(ICON, icon)
            .putLong(FROM_TIME_IN_MILLIS, fromTimeInMillis)
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(MyCoroutineWorker::class.java)
            .setInitialDelay(fromTimeInMillis - Calendar.getInstance().timeInMillis, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(alert.id.toString())
            .build()

        WorkManager
            .getInstance(context)
            .enqueueUniqueWork("${alert.id}", ExistingWorkPolicy.REPLACE, oneTimeWorkRequest)
    }

    fun removeWork(tag: String, context: Context) {
        val worker = WorkManager.getInstance(context)
        worker.cancelAllWorkByTag(tag)
    }
}
