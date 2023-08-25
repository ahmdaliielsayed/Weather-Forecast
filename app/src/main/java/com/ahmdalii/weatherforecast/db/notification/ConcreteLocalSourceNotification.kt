package com.ahmdalii.weatherforecast.db.notification

import android.content.Context
import android.content.res.Resources.NotFoundException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ahmdalii.weatherforecast.db.AppDataBase
import com.ahmdalii.weatherforecast.model.MyAlert
import com.ahmdalii.weatherforecast.utils.AppConstants.ZERO

class ConcreteLocalSourceNotification(context: Context) : LocalSourceNotification {

    private val dao: NotificationDAO?
    override val alertList: LiveData<List<MyAlert>>

    init {
        val db: AppDataBase = AppDataBase.getInstance(context)
        dao = db.notificationDAO()
        alertList = dao?.alertList ?: MutableLiveData<List<MyAlert>>().apply { value = emptyList() }
    }

    override fun insertAlert(alert: MyAlert): Long {
        return dao?.insertAlert(alert) ?: ZERO
    }

    override fun deleteAlert(alert: MyAlert) {
        dao?.deleteAlert(alert)
    }

    override fun deleteAlert(id: Long) {
        dao?.deleteAlert(id)
    }

    override fun getAlert(id: Long): MyAlert {
        return dao?.getAlert(id) ?: throw NotFoundException("Alert not found for id: $id")
    }
}
