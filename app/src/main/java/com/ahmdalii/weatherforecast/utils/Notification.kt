package com.ahmdalii.weatherforecast.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.ahmdalii.weatherforecast.ui.HomeActivity
import com.ahmdalii.weatherforecast.utils.AppConstants.getIcon

class Notification(
    context: Context,
    var description: String,
    var icon: String,
    var title: String
) : ContextWrapper(context) {

    private val CHANNEL_ID = "Channel ID"
    private val CHANNEL_NAME = "Channel Name"
    private val CHANNEL_DESCRIPTION = "Channel Name"

    private var mManager: NotificationManager? = null
    private var uniqueInt = (System.currentTimeMillis() and 0xfffffff).toInt()

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val channel =
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)

        channel.enableVibration(true)
        channel.description = CHANNEL_DESCRIPTION
        getManager()?.createNotificationChannel(channel)
    }

    fun getManager(): NotificationManager? {
        if (mManager == null) {
            mManager = getSystemService(NotificationManager::class.java)
        }
        return mManager
    }

    fun getChannelNotification(): NotificationCompat.Builder {
        val intent = Intent(applicationContext, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

//        intent.putExtra(LOCATION_LATITUDE, weatherModel.lat)
//        intent.putExtra(LOCATION_LONGITUDE, weatherModel.lon)

        val bitmap = BitmapFactory.decodeResource(applicationContext.resources, getIcon(icon))

        @SuppressLint("UnspecifiedImmutableFlag")
        val pendingIntent = PendingIntent.getActivity(
            this,
            uniqueInt,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        return NotificationCompat.Builder(
            applicationContext,
            CHANNEL_ID
        )
            .setContentTitle(title)
            .setContentText(description) // getText(R.string.open_dialogue)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // you can delete this line
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSmallIcon(getIcon(icon))
            .setLargeIcon(bitmap)
    }
}