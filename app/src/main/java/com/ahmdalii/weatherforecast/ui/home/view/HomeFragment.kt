package com.ahmdalii.weatherforecast.ui.home.view

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.databinding.FragmentHomeBinding
import com.ahmdalii.weatherforecast.db.weather.ConcreteLocalSource
import com.ahmdalii.weatherforecast.model.Weather
import com.ahmdalii.weatherforecast.model.WeatherModel
import com.ahmdalii.weatherforecast.network.WeatherClient
import com.ahmdalii.weatherforecast.ui.home.repo.HomeRepo
import com.ahmdalii.weatherforecast.ui.home.viewmodel.HomeViewModel
import com.ahmdalii.weatherforecast.ui.home.viewmodel.HomeViewModelFactory
import com.ahmdalii.weatherforecast.ui.map.view.MapsActivity
import com.ahmdalii.weatherforecast.utils.AppConstants
import com.ahmdalii.weatherforecast.utils.AppConstants.IMG_URL
import com.ahmdalii.weatherforecast.utils.AppConstants.INITIAL_DIALOG
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_METHOD_GPS
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_METHOD_MAP
import com.ahmdalii.weatherforecast.utils.AppConstants.WIND_SPEED_UNIT_M_P_S
import com.ahmdalii.weatherforecast.utils.AppConstants.checkLocationPermissions
import com.ahmdalii.weatherforecast.utils.AppConstants.getDateTime
import com.ahmdalii.weatherforecast.utils.AppConstants.isInternetAvailable
import com.ahmdalii.weatherforecast.utils.AppConstants.isLocationEnabled
import com.ahmdalii.weatherforecast.utils.ConnectionLiveData
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import kotlin.math.roundToInt

class HomeFragment : Fragment() {

    private lateinit var homeViewModelFactory: HomeViewModelFactory
    private lateinit var viewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    private lateinit var myView: View
    private lateinit var dialog: Dialog
    private var isAllPermissionsGranted: Boolean = false

    private lateinit var radioBtn: RadioButton
    private lateinit var notificationSwitch: SwitchCompat

    private lateinit var homeHourlyAdapter: HomeHourlyAdapter
    private lateinit var linearHomeHourlyLayoutManager: LinearLayoutManager
    private lateinit var homeDailyAdapter: HomeDailyAdapter
    private lateinit var linearHomeDailyLayoutManager: LinearLayoutManager

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.myView = view
        gettingViewModelReady()
        initHourlyRecyclerView()
        initDailyRecyclerView()
        listenerOnNetwork()
        if (!viewModel.isFirstTimeCompleted(myView.context)) {
            configureDialog()
        }
    }

    private fun listenerOnNetwork() {
        ConnectionLiveData(myView.context).observe(this, {
            if (viewModel.isFirstTimeCompleted(myView.context)) {
                if (it) {
                    getWeatherDataOverNetwork()
                } else {
                    viewModel.getAllStoredWeatherModel(myView.context).observe(this, { weatherModel ->
                        if (weatherModel != null) {
                            renderDataOnScreen(weatherModel)
                            Snackbar.make(myView, getString(R.string.connection_lost), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show()
                        }
                    })
                }
            }
        })
    }

    private fun gettingViewModelReady() {
        homeViewModelFactory = HomeViewModelFactory(
            HomeRepo.getInstance(
                WeatherClient.getInstance(), ConcreteLocalSource(myView.context)
            )
        )
        viewModel = ViewModelProvider(this, homeViewModelFactory)[HomeViewModel::class.java]

        viewModel.errorMsgResponse.observe(viewLifecycleOwner, {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(myView.context, it, Toast.LENGTH_LONG).show()
        })
        viewModel.showProgressBar.observe(viewLifecycleOwner, {
            if (it) {
                binding.progressBar.visibility = View.VISIBLE
            }
        })
        viewModel.weatherModelResponse.observe(viewLifecycleOwner, {
            binding.progressBar.visibility = View.GONE
            binding.animationView.visibility = View.GONE
            binding.animationView.loop(false)
            binding.showWeatherData.visibility = View.VISIBLE
            renderDataOnScreen(it)
        })
    }

    private fun renderDataOnScreen(it: WeatherModel) {
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
        binding.txtViewCurrentDateTime.text = getDateTime(it.current.dt, "EEE, MMM d, yyyy hh:mm a")
        homeHourlyAdapter.setDataToAdapter(it.hourly!!)
        homeDailyAdapter.setDataToAdapter(it.daily!!)
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

    private fun initHourlyRecyclerView() {
        homeHourlyAdapter = HomeHourlyAdapter(myView.context, emptyList(), viewModel, viewLifecycleOwner)
        linearHomeHourlyLayoutManager = LinearLayoutManager(myView.context, RecyclerView.HORIZONTAL, false)
        binding.recyclerViewHourly.apply {
            adapter = homeHourlyAdapter
            layoutManager = linearHomeHourlyLayoutManager
        }
    }

    private fun initDailyRecyclerView() {
        homeDailyAdapter = HomeDailyAdapter(myView.context, emptyList(), viewModel, viewLifecycleOwner)
        linearHomeDailyLayoutManager = LinearLayoutManager(myView.context, RecyclerView.VERTICAL, false)
        binding.recyclerViewDaily.apply {
            adapter = homeDailyAdapter
            layoutManager = linearHomeDailyLayoutManager
        }
    }

    private fun setCurrentWeatherDescription(weatherList: List<Weather>) {
        var weatherDescription = ""
        for (weatherObject in weatherList) {
            weatherDescription += weatherObject.description + "\n"
        }
        binding.txtViewCurrentWeatherDescription.text = weatherDescription
    }

    private fun setCurrentLocation() {
        viewModel.getCurrentLocation(myView.context)
        viewModel.currentLocation.observe(viewLifecycleOwner, {
            binding.txtViewGovernorate.text = it[0]
            binding.txtViewLocality.text = it[1]
        })
    }

    private fun setCurrentTempDiscrimination() {
        viewModel.getCurrentTempMeasurementUnit(myView.context)
        viewModel.currentTempMeasurementUnit.observe(viewLifecycleOwner, {
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
        viewModel.getWindSpeedMeasurementUnit(myView.context)
        viewModel.windSpeedMeasurementUnit.observe(viewLifecycleOwner, {
            when {
                it.isNullOrBlank() || it.equals(WIND_SPEED_UNIT_M_P_S) -> {
                    binding.txtViewWindSpeedDiscrimination.text = getString(R.string.m_p_s)
                }
                else -> {
                    binding.txtViewWindSpeedDiscrimination.text = getString(R.string.m_p_h)
                }
            }
        })
    }

    private fun setCurrentWeatherIcon(iconURL: String) {
        Glide
            .with(myView.context)
            .load("$IMG_URL${iconURL}@4x.png")
            .into(binding.imgViewCurrentWeatherIcon)

        Glide
            .with(myView.context)
            .load("$IMG_URL${iconURL}@4x.png")
            .into(binding.imgViewFeelsLikeIcon)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun configureDialog() {
        dialog = Dialog(myView.context)

        dialog.setContentView(R.layout.initial_setup_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val radioGroup = dialog.findViewById<RadioGroup>(R.id.radioGroupLocation)
        notificationSwitch = dialog.findViewById(R.id.notificationSwitch)
        val btnOk = dialog.findViewById<Button>(R.id.btnOk)

        /*radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val radioBtn = dialog.findViewById<RadioButton>(checkedId)
            when (checkedId) {
                R.id.radioBtnGPS -> {
                    // check permission
                    Log.d("asdfg:A", radioBtn.text.toString())
                }
                R.id.radioBtnMap -> {
                    // open map
                    Log.d("asdfg:B", radioBtn.text.toString())
                }
            }
        }*/

        dialog.findViewById<RadioButton>(R.id.radioBtnMap).setOnClickListener {
            val intent = Intent(myView.context, MapsActivity::class.java)
            intent.putExtra(AppConstants.COMING_FROM, INITIAL_DIALOG)
            startActivity(intent)
        }

        btnOk.setOnClickListener {
            val checkedRadioButtonId = radioGroup.checkedRadioButtonId
            radioBtn = dialog.findViewById(checkedRadioButtonId)

            checkPermissionsToDismissDialog()
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun checkPermissionsToDismissDialog() {
        if (checkLocationPermissions(myView.context)) {
            if (radioBtn.text.equals(getString(R.string.gps))) {
                saveLocationMethod(myView.context, LOCATION_METHOD_GPS)

                if (isLocationEnabled(myView.context)) {
                    saveUpdateGPSLocation()
                    viewModel.isNotificationChecked(myView.context, notificationSwitch.isChecked)
                    getWeather()
                } else {
                    Toast.makeText(myView.context, R.string.open_gps, Toast.LENGTH_LONG).show()
                }
            } else {
                saveLocationMethod(myView.context, LOCATION_METHOD_MAP)
                getWeather()
            }
        } else {
            requestLocationPermissions()
        }
    }

    private fun getWeather() {
        if (isInternetAvailable(myView.context)) {
            viewModel.firstTimeCompleted(myView.context)
            getWeatherDataOverNetwork()
            dialog.dismiss()
        } else {
            Toast.makeText(myView.context, R.string.first_time_fetch, Toast.LENGTH_LONG).show()
        }
    }

    private fun saveLocationMethod(context: Context, locationMethod: String) {
        viewModel.saveLocationMethod(context, locationMethod)
    }

    private fun saveUpdateGPSLocation() {
        viewModel.saveUpdateLocation(myView.context)
    }

    private fun requestLocationPermissions() {
        requestLocationPermissions.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    private val requestLocationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                if (it.value) {
                    // Permission is granted. Continue the action or workflow in your app.
                    Log.d("asdfg:", "${it.key} granted")
                    isAllPermissionsGranted = true
                    if (!isInternetAvailable(myView.context)) {
                        Toast.makeText(myView.context, R.string.first_time_fetch, Toast.LENGTH_LONG).show()
                    }
                } else {
                    isAllPermissionsGranted = false
                    AppConstants.showAlert(
                        myView.context,
                        R.string.warning,
                        "${it.key} ${getString(R.string.cancelled)} \n\n${getString(R.string.permission_required)}",
                        R.drawable.ic_warning
                    )
                }
            }

            checkPermissionsToDismissDialog()
        }

    private fun getWeatherDataOverNetwork() {
        binding.progressBar.visibility = View.VISIBLE
        viewModel.observeOnSharedPref(myView.context)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.unRegisterOnSharedPreferenceChangeListener()
    }
}