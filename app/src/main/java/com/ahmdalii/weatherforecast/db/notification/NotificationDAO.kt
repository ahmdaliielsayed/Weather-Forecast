package com.ahmdalii.weatherforecast.db.notification

import androidx.lifecycle.LiveData
import androidx.room.*
import com.ahmdalii.weatherforecast.model.MyAlert

@Dao
interface NotificationDAO {

    @get:Query("SELECT * FROM MyAlerts")
    val alertList: LiveData<List<MyAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAlert(alert: MyAlert): Long

    @Delete
    fun deleteAlert(alert: MyAlert)

    @Query("DELETE FROM MyAlerts where id = :id")
    fun deleteAlert(id: Long)

    @Query("select * from MyAlerts where id = :id")
    fun getAlert(id: Long): MyAlert
}
