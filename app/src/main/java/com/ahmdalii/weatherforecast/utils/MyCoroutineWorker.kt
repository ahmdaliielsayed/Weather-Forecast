package com.ahmdalii.weatherforecast.utils

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.db.notification.ConcreteLocalSourceNotification
import com.ahmdalii.weatherforecast.db.weather.ConcreteLocalSource
import com.ahmdalii.weatherforecast.model.MyAlert
import com.ahmdalii.weatherforecast.ui.notification.repo.NotificationRepo
import com.ahmdalii.weatherforecast.ui.notification.repo.NotificationRepoInterface
import com.ahmdalii.weatherforecast.ui.notification.view.DialogActivity
import com.ahmdalii.weatherforecast.utils.AppConstants.ALARM_CHECKED
import com.ahmdalii.weatherforecast.utils.AppConstants.DESCRIPTION
import com.ahmdalii.weatherforecast.utils.AppConstants.FROM_TIME_IN_MILLIS
import com.ahmdalii.weatherforecast.utils.AppConstants.ICON
import com.ahmdalii.weatherforecast.utils.AppConstants.MY_ALERT
import com.ahmdalii.weatherforecast.utils.AppConstants.NOTIFICATION_CHECKED
import com.ahmdalii.weatherforecast.utils.AppConstants.convertToMyAlert
import com.ahmdalii.weatherforecast.utils.AppConstants.openNotification
import com.ahmdalii.weatherforecast.utils.WorkRequestManager.removeWork
import kotlinx.coroutines.*
import java.util.*

class MyCoroutineWorker(private val context: Context, parameters: WorkerParameters): CoroutineWorker(context, parameters) {

    private lateinit var notificationRepository: NotificationRepoInterface

    override suspend fun doWork(): Result {
        CoroutineScope(Dispatchers.IO).launch {
            val myAlert = convertToMyAlert(inputData.getString(MY_ALERT)!!)
            val description = inputData.getString(DESCRIPTION)
            val icon = inputData.getString(ICON)
            val fromTimeInMillis = inputData.getLong(FROM_TIME_IN_MILLIS, 0L)

            notificationRepository =
                NotificationRepo.getInstance(ConcreteLocalSourceNotification(applicationContext), ConcreteLocalSource(applicationContext))

            if (checkTime(myAlert)) {
                if (notificationRepository.getNotificationChecked(context) || myAlert.alarm_or_notification == NOTIFICATION_CHECKED){
                    openNotification(context, myAlert, description!!, icon!!, context.getString(R.string.app_name))
                }

                if (myAlert.alarm_or_notification == ALARM_CHECKED) {
                    if (Settings.canDrawOverlays(context)) {
                        withContext(Dispatchers.Main) {
                            val intent = Intent(context, DialogActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_INCLUDE_STOPPED_PACKAGES or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            intent.putExtra(DESCRIPTION, description)
                            intent.putExtra(ICON, icon)
                            context.startActivity(intent)
                        }
                    }
                }

                WorkRequestManager.createWorkRequest(
                    myAlert,
                    description.toString(),
                    icon.toString(),
                    context,
                    (fromTimeInMillis + 86400000)
                )
            } else {
                notificationRepository.deleteAlert(myAlert.id!!)
                removeWork("${myAlert.id}", context)
            }
        }

        return Result.success()
    }

    private fun checkTime(alert: MyAlert): Boolean {
        val currentTimeInMillis = Calendar.getInstance().timeInMillis
        return currentTimeInMillis >= alert.startDate && currentTimeInMillis <= alert.endDate
    }
}