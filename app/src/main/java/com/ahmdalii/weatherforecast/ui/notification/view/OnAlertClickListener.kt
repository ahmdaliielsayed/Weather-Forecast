package com.ahmdalii.weatherforecast.ui.notification.view

import com.ahmdalii.weatherforecast.model.MyAlert

interface OnAlertClickListener {

    fun onRemoveClick(myAlert: MyAlert)
    fun onAlertClick(myAlert: MyAlert)
}
