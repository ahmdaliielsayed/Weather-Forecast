package com.ahmdalii.weatherforecast.ui.favorite.repo

import android.content.Context
import androidx.lifecycle.LiveData
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.db.favorite.LocalSourceFavorite
import com.ahmdalii.weatherforecast.model.FavoritePlace
import com.ahmdalii.weatherforecast.model.WeatherModel
import com.ahmdalii.weatherforecast.network.RemoteSource
import com.ahmdalii.weatherforecast.utils.AppConstants
import com.ahmdalii.weatherforecast.utils.AppConstants.APPLICATION_LANGUAGE
import com.ahmdalii.weatherforecast.utils.AppConstants.MEASUREMENT_UNIT
import com.ahmdalii.weatherforecast.utils.AppConstants.MEASUREMENT_UNIT_STANDARD
import com.ahmdalii.weatherforecast.utils.AppConstants.SETTING_FILE
import com.ahmdalii.weatherforecast.utils.AppConstants.WIND_SPEED_UNIT
import com.ahmdalii.weatherforecast.utils.AppConstants.WIND_SPEED_UNIT_M_P_S
import com.ahmdalii.weatherforecast.utils.AppConstants.convertWindSpeedToMPH
import com.ahmdalii.weatherforecast.utils.AppConstants.convertWindSpeedToMPS
import com.ahmdalii.weatherforecast.utils.AppConstants.getDisplayCurrentLanguage
import com.ahmdalii.weatherforecast.utils.AppConstants.getPlaceName
import com.ahmdalii.weatherforecast.utils.AppSharedPref
import retrofit2.Response

class FavoriteRepo private constructor(
    private var remoteSource: RemoteSource,
    private var localSourceFavorite: LocalSourceFavorite,
) :
    FavoriteRepoInterface {

    companion object {
        private var instance: FavoriteRepoInterface? = null
        fun getInstance(remoteSource: RemoteSource, localSourceFavorite: LocalSourceFavorite): FavoriteRepoInterface {
            return instance ?: FavoriteRepo(remoteSource, localSourceFavorite)
        }
    }

    override val allFavoritePlaces: LiveData<List<FavoritePlace>>
        get() = localSourceFavorite.allFavoritePlacesList

    override fun deleteFavoritePlace(favoritePlace: FavoritePlace) {
        localSourceFavorite.deleteFavoritePlace(favoritePlace)
    }

    override fun insertFavoritePlace(favoritePlace: FavoritePlace) {
        localSourceFavorite.insertFavoritePlace(favoritePlace)
    }

    override suspend fun getCurrentWeatherOverNetwork(context: Context, favoritePlace: FavoritePlace): Response<WeatherModel> {
        val language =
            AppSharedPref.getInstance(context, SETTING_FILE)
                .getStringValue(APPLICATION_LANGUAGE, getDisplayCurrentLanguage())
        val measurementUnit = AppSharedPref.getInstance(context, SETTING_FILE)
            .getStringValue(MEASUREMENT_UNIT, MEASUREMENT_UNIT_STANDARD)

        val placeName = getPlaceName(context, favoritePlace.latitude, favoritePlace.longitude)
        favoritePlace.adminArea = placeName.adminArea ?: context.getString(R.string.unknown_adminArea)
        favoritePlace.locality = placeName.locality ?: context.getString(R.string.unknown_locality)
        updateCurrentPlaceName(favoritePlace)

        val currentWeatherOverNetwork = remoteSource.getCurrentWeatherOverNetwork(
            favoritePlace.latitude.toFloat(),
            favoritePlace.longitude.toFloat(),
            language,
            measurementUnit,
        )

        val windSpeedUnit = AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(
            WIND_SPEED_UNIT,
            WIND_SPEED_UNIT_M_P_S,
        )
        val measurementUnitValue = AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(
            MEASUREMENT_UNIT,
            MEASUREMENT_UNIT_STANDARD,
        )
        if (measurementUnitValue == AppConstants.MEASUREMENT_UNIT_IMPERIAL && windSpeedUnit == WIND_SPEED_UNIT_M_P_S) {
            currentWeatherOverNetwork.body()?.current?.windSpeed =
                convertWindSpeedToMPS(currentWeatherOverNetwork.body()?.current?.windSpeed)
        } else if ((measurementUnitValue == AppConstants.MEASUREMENT_UNIT_METRIC && windSpeedUnit == AppConstants.WIND_SPEED_UNIT_M_P_H) ||
            (measurementUnitValue == MEASUREMENT_UNIT_STANDARD && windSpeedUnit == AppConstants.WIND_SPEED_UNIT_M_P_H)
        ) {
            currentWeatherOverNetwork.body()?.current?.windSpeed = convertWindSpeedToMPH(currentWeatherOverNetwork.body()?.current?.windSpeed)
        }

        return currentWeatherOverNetwork
    }

    override fun updateCurrentPlaceName(favoritePlace: FavoritePlace) {
        localSourceFavorite.updateFavoritePlace(favoritePlace)
    }

    override fun getCurrentTempMeasurementUnit(context: Context): String {
        return AppSharedPref.getInstance(context, SETTING_FILE)
            .getStringValue(MEASUREMENT_UNIT, MEASUREMENT_UNIT_STANDARD)
    }

    override fun getWindSpeedMeasurementUnit(context: Context): String {
        return AppSharedPref.getInstance(context, SETTING_FILE)
            .getStringValue(WIND_SPEED_UNIT, WIND_SPEED_UNIT_M_P_S)
    }

    override fun getLanguage(context: Context): String {
        return AppSharedPref.getInstance(context, SETTING_FILE).getStringValue(APPLICATION_LANGUAGE, getDisplayCurrentLanguage())
    }
}
