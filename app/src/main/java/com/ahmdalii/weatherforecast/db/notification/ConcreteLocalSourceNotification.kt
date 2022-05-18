package com.ahmdalii.weatherforecast.db.notification

import android.content.Context
import androidx.lifecycle.LiveData
import com.ahmdalii.weatherforecast.db.AppDataBase
import com.ahmdalii.weatherforecast.model.MyAlert

class ConcreteLocalSourceNotification(context: Context) : LocalSourceNotification {

    private val dao: NotificationDAO?
    override val alertList: LiveData<List<MyAlert>>

    init {
        val db: AppDataBase = AppDataBase.getInstance(context)
        dao = db.notificationDAO()
        alertList = dao?.alertList!!
    }

    override fun insertAlert(alert: MyAlert): Long {
        return dao?.insertAlert(alert)!!
    }

    override fun deleteAlert(alert: MyAlert) {
        dao?.deleteAlert(alert)
    }

    override fun deleteAlert(id: Long) {
        dao?.deleteAlert(id)
    }

    override fun getAlert(id: Long): MyAlert {
        return dao?.getAlert(id)!!
    }
}