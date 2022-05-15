package com.ahmdalii.weatherforecast.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "Alert")
class Alert(
    @PrimaryKey
    @ColumnInfo(name = "sender_name")
    @SerializedName("sender_name")
    val senderName: String,

    @ColumnInfo(name = "event")
    val event: String,

    @ColumnInfo(name = "start")
    val start: Int,

    @ColumnInfo(name = "end")
    val end: Int,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "tags")
    val tags: List<String> = emptyList()
)