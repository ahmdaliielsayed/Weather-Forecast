package com.ahmdalii.weatherforecast.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "FavoritePlace")
data class FavoritePlace(
    val latitude: Double,
    val longitude: Double,
    var adminArea: String,
    @PrimaryKey
    var locality: String
): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(adminArea)
        parcel.writeString(locality)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FavoritePlace> {
        override fun createFromParcel(parcel: Parcel): FavoritePlace {
            return FavoritePlace(parcel)
        }

        override fun newArray(size: Int): Array<FavoritePlace?> {
            return arrayOfNulls(size)
        }
    }
}