package com.ahmdalii.weatherforecast.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ahmdalii.weatherforecast.db.favorite.FavoriteDAO
import com.ahmdalii.weatherforecast.db.notification.NotificationDAO
import com.ahmdalii.weatherforecast.db.weather.WeatherModelDAO
import com.ahmdalii.weatherforecast.model.FavoritePlace
import com.ahmdalii.weatherforecast.model.MyAlert
import com.ahmdalii.weatherforecast.model.WeatherModel

@Database(entities = [WeatherModel::class, FavoritePlace::class, MyAlert::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDataBase : RoomDatabase() {

    abstract fun weatherModelDAO(): WeatherModelDAO?
    abstract fun favoriteDAO(): FavoriteDAO?
    abstract fun notificationDAO(): NotificationDAO?

    companion object {
        private var instance: AppDataBase? = null

        @Synchronized
        fun getInstance(context: Context): AppDataBase {
            return instance ?: Room.databaseBuilder(context.applicationContext, AppDataBase::class.java, "weather")
                .build()
        }
    }
}
