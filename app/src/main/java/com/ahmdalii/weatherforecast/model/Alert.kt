package com.ahmdalii.weatherforecast.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "Alert")
class Alert {

    @PrimaryKey
    @ColumnInfo(name = "sender_name")
    @SerializedName("sender_name")
    private lateinit var senderName: String
    @ColumnInfo(name = "event")
    private lateinit var event: String
    @ColumnInfo(name = "start")
    private var start: Int = 0
    @ColumnInfo(name = "end")
    private var end: Int = 0
    @ColumnInfo(name = "description")
    private lateinit var description: String
    @ColumnInfo(name = "tags")
    private var tags: List<String> = emptyList()

    fun getSenderName(): String {
        return senderName
    }
    fun setSenderName(senderName: String) {
        this.senderName = senderName
    }

    fun getEvent(): String {
        return event
    }
    fun setEvent(event: String) {
        this.event = event
    }

    fun getStart(): Int {
        return start
    }
    fun setStart(start: Int) {
        this.start = start
    }

    fun getEnd(): Int {
        return end
    }
    fun setEnd(end: Int) {
        this.end = end
    }

    fun getDescription(): String {
        return description
    }
    fun setDescription(description: String) {
        this.description = description
    }

    fun getTags(): List<String> {
        return tags
    }
    fun setTags(tags: List<String>) {
        this.tags = tags
    }
}