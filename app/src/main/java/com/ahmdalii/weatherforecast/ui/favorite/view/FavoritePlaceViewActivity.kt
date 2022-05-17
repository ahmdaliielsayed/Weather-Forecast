package com.ahmdalii.weatherforecast.ui.favorite.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.databinding.ActivityFavoritePlaceViewBinding
import com.ahmdalii.weatherforecast.db.favorite.ConcreteLocalSourceFavorite
import com.ahmdalii.weatherforecast.model.FavoritePlace
import com.ahmdalii.weatherforecast.model.Weather
import com.ahmdalii.weatherforecast.model.WeatherModel
import com.ahmdalii.weatherforecast.network.WeatherClient
import com.ahmdalii.weatherforecast.ui.favorite.repo.FavoriteRepo
import com.ahmdalii.weatherforecast.ui.favorite.viewmodel.FavoriteViewModel
import com.ahmdalii.weatherforecast.ui.favorite.viewmodel.FavoriteViewModelFactory
import com.ahmdalii.weatherforecast.ui.home.view.HomeHourlyAdapter
import com.ahmdalii.weatherforecast.utils.AppConstants
import com.ahmdalii.weatherforecast.utils.AppConstants.FAVORITE_KEY
import com.ahmdalii.weatherforecast.utils.AppConstants.getPlaceName
import com.ahmdalii.weatherforecast.utils.ConnectionLiveData
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlin.math.roundToInt

class FavoritePlaceViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritePlaceViewBinding

    private lateinit var favoritePlacesViewModelFactory: FavoriteViewModelFactory
    private lateinit var viewModel: FavoriteViewModel

    private lateinit var favoriteHourlyAdapter: FavoriteHourlyAdapter
    private lateinit var linearHomeHourlyLayoutManager: LinearLayoutManager
    private lateinit var favoriteDailyAdapter: FavoriteDailyAdapter
    private lateinit var linearFavoriteDailyLayoutManager: LinearLayoutManager

    private lateinit var favoritePlace: FavoritePlace

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFavoritePlaceViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        favoritePlace = intent.getParcelableExtra(FAVORITE_KEY)!!

        gettingViewModelReady()
        initHourlyRecyclerView()
        initDailyRecyclerView()
        listenerOnNetwork()
    }

    private fun listenerOnNetwork() {
        ConnectionLiveData(this).observe(this, {
            if (!it) {
                Snackbar.make(findViewById(android.R.id.content), getString(R.string.connection_lost), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
//                Toast.makeText(this, getString(R.string.connection_lost), Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun gettingViewModelReady() {
        favoritePlacesViewModelFactory = FavoriteViewModelFactory(
            FavoriteRepo.getInstance(WeatherClient.getInstance(), ConcreteLocalSourceFavorite(this))
        )
        viewModel = ViewModelProvider(this, favoritePlacesViewModelFactory)[FavoriteViewModel::class.java]
        viewModel.getCurrentWeatherOverNetwork(this, favoritePlace)

        viewModel.errorMsgResponse.observe(this, {
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        })
        viewModel.animationView.observe(this, {
            if (it) {
                binding.animationView.visibility = View.GONE
                binding.animationView.loop(false)
            }
        })
        viewModel.weatherModelResponse.observe(this, {
            renderDataOnScreen(it)
        })
    }

    private fun initHourlyRecyclerView() {
        favoriteHourlyAdapter = FavoriteHourlyAdapter(this, emptyList(), viewModel, this)
        linearHomeHourlyLayoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        binding.recyclerViewHourly.apply {
            adapter = favoriteHourlyAdapter
            layoutManager = linearHomeHourlyLayoutManager
        }
    }

    private fun initDailyRecyclerView() {
        favoriteDailyAdapter = FavoriteDailyAdapter(this, emptyList(), viewModel, this)
        linearFavoriteDailyLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.recyclerViewDaily.apply {
            adapter = favoriteDailyAdapter
            layoutManager = linearFavoriteDailyLayoutManager
        }
    }

    private fun renderDataOnScreen(it: WeatherModel) {
        binding.showWeatherData.visibility = View.VISIBLE

        setCurrentLocation()
        setCurrentTempDiscrimination()
        setWindSpeedDiscrimination()
        binding.txtViewCurrentTemp.text = if (it.current.temp.rem(100) >= 50) {
            "${it.current.temp.toInt().plus(1)}"
        } else {
            "${it.current.temp.toInt()}"
        }
        setCurrentWeatherDescription(it.current.weather)
        setCurrentWeatherIcon(it.current.weather[0].icon)
        binding.txtViewCurrentDateTime.text =
            AppConstants.getDateTime(it.current.dt, "EEE, MMM d, yyyy hh:mm a", viewModel.getLanguage(this))
        favoriteHourlyAdapter.setDataToAdapter(it.hourly!!)
        favoriteDailyAdapter.setDataToAdapter(it.daily!!)
        binding.txtViewPressure.text = it.current.pressure.toString().plus(" hPa")
        binding.txtViewHumidity.text = it.current.humidity.toString().plus(" %")
        binding.txtViewWindSpeed.text = ((it.current.windSpeed * 100.0).roundToInt() / 100.0).toString().plus(" ")
        binding.txtViewClouds.text = it.current.clouds.toString().plus(" %")
        binding.txtViewUVI.text = it.current.uvi.toString()
        binding.txtViewVisibility.text = it.current.visibility.toString().plus(" ").plus(getString(R.string.metres))
        binding.txtViewFeelsLikeTemp.text = if (it.current.feelsLike.rem(100) >= 50) {
            "${it.current.feelsLike.toInt().plus(1)}"
        } else {
            "${it.current.feelsLike.toInt()}"
        }
    }

    private fun setCurrentLocation() {
        val placeName = getPlaceName(this, favoritePlace.latitude, favoritePlace.longitude)
        binding.txtViewGovernorate.text = placeName.adminArea ?: getString(R.string.unknown_adminArea)
        binding.txtViewLocality.text = placeName.locality ?: getString(R.string.unknown_locality)
    }

    private fun setCurrentTempDiscrimination() {
        viewModel.getCurrentTempMeasurementUnit(this)
        viewModel.currentTempMeasurementUnit.observe(this, {
            when {
                it.isNullOrBlank() -> {
                    binding.txtViewCurrentTempDiscrimination.text = getString(R.string.temp_kelvin)
                    binding.txtViewFeelsLikeDiscrimination.text = getString(R.string.temp_kelvin)
                }
                it.equals("metric") -> {
                    binding.txtViewCurrentTempDiscrimination.text = getString(R.string.temp_celsius)
                    binding.txtViewFeelsLikeDiscrimination.text = getString(R.string.temp_celsius)
                }
                it.equals("imperial") -> {
                    binding.txtViewCurrentTempDiscrimination.text = getString(R.string.temp_fahrenheit)
                    binding.txtViewFeelsLikeDiscrimination.text = getString(R.string.temp_fahrenheit)
                }
            }
        })
    }

    private fun setWindSpeedDiscrimination() {
        viewModel.getWindSpeedMeasurementUnit(this)
        viewModel.windSpeedMeasurementUnit.observe(this, {
            when {
                it.isNullOrBlank() || it.equals(AppConstants.WIND_SPEED_UNIT_M_P_S) -> {
                    binding.txtViewWindSpeedDiscrimination.text = getString(R.string.m_p_s)
                }
                else -> {
                    binding.txtViewWindSpeedDiscrimination.text = getString(R.string.m_p_h)
                }
            }
        })
    }

    private fun setCurrentWeatherDescription(weatherList: List<Weather>) {
        var weatherDescription = ""
        for (weatherObject in weatherList) {
            weatherDescription += weatherObject.description + "\n"
        }
        binding.txtViewCurrentWeatherDescription.text = weatherDescription
    }

    private fun setCurrentWeatherIcon(iconURL: String) {
        Glide
            .with(this)
            .load("${AppConstants.IMG_URL}${iconURL}@4x.png")
            .into(binding.imgViewCurrentWeatherIcon)

        Glide
            .with(this)
            .load("${AppConstants.IMG_URL}${iconURL}@4x.png")
            .into(binding.imgViewFeelsLikeIcon)
    }
}