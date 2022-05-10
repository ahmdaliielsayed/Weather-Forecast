package com.ahmdalii.weatherforecast.ui.home.view

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.databinding.FragmentHomeBinding
import com.ahmdalii.weatherforecast.db.ConcreteLocalSource
import com.ahmdalii.weatherforecast.model.Weather
import com.ahmdalii.weatherforecast.model.WeatherModel
import com.ahmdalii.weatherforecast.network.WeatherClient
import com.ahmdalii.weatherforecast.ui.home.repo.HomeRepo
import com.ahmdalii.weatherforecast.ui.home.viewmodel.HomeViewModel
import com.ahmdalii.weatherforecast.ui.home.viewmodel.HomeViewModelFactory
import com.ahmdalii.weatherforecast.utils.AppConstants
import com.ahmdalii.weatherforecast.utils.AppConstants.IMG_URL
import com.ahmdalii.weatherforecast.utils.AppConstants.WAIT_FIRST_TIME
import com.ahmdalii.weatherforecast.utils.AppConstants.getDateTime
import com.ahmdalii.weatherforecast.utils.ConnectionLiveData
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar

class HomeFragment : Fragment() {

    private lateinit var homeViewModelFactory: HomeViewModelFactory
    private lateinit var viewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    private lateinit var myView: View
    private lateinit var dialog: Dialog
    private var isAllPermissionsGranted: Boolean = false
    private var firstConnectionListener: Boolean = false

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
//        val root: View = binding.root
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.myView = view
        gettingViewModelReady()
        initHourlyRecyclerView()
        initDailyRecyclerView()
        listenerOnNetwork()
//        configureDialog()
        if (viewModel.isFirstTimeComplete(myView.context)) {
            listenerOnNetwork()
        } else {
            configureDialog()
        }
    }

    private fun listenerOnNetwork() {
        ConnectionLiveData(myView.context).observe(this, {
            if (it) {
                if (firstConnectionListener) {
                    // get data from network
                    getWeatherDataOverNetwork()
                } else {
                    firstConnectionListener = true
                }
            } else {
                viewModel.getAllStoredMovies().observe(this, { weatherModel ->
                    renderDataOnScreen(weatherModel)
                })
                Snackbar.make(myView, getString(R.string.connection_lost), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
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
        viewModel.errorMsgResponse.observe(viewLifecycleOwner, Observer {
            Toast.makeText(myView.context, it, Toast.LENGTH_LONG).show()
        })
        viewModel.weatherModelResponse.observe(viewLifecycleOwner, Observer {
            binding.progressBar.visibility = View.GONE
            renderDataOnScreen(it)
        })
    }

    private fun renderDataOnScreen(it: WeatherModel) {
        setCurrentLocation()
        setCurrentTempAndWindSpeedDiscrimination()
        binding.txtViewCurrentTemp.text = if (it.getCurrent().temp.toInt() % 100 >= 50) {
            "${it.getCurrent().temp.toInt().plus(1)}"
        } else {
            "${it.getCurrent().temp.toInt()}"
        }
        setCurrentWeatherDescription(it.getCurrent().weather)
        setCurrentWeatherIcon(it.getCurrent().weather[0].icon)
        binding.txtViewCurrentDateTime.text = getDateTime(it.getCurrent().dt, "EEE, MMM d, yyyy hh:mm a")
        homeHourlyAdapter.setDataToAdapter(it.getHourly())
        homeDailyAdapter.setDataToAdapter(it.getDaily())
        binding.txtViewPressure.text = it.getCurrent().pressure.toString().plus(" hPa")
        binding.txtViewHumidity.text = it.getCurrent().humidity.toString().plus(" %")
        binding.txtViewWindSpeed.text = it.getCurrent().windSpeed.toString().plus(" ")
        binding.txtViewClouds.text = it.getCurrent().clouds.toString().plus(" %")
        binding.txtViewUVI.text = it.getCurrent().uvi.toString()
        binding.txtViewVisibility.text = it.getCurrent().visibility.toString().plus(" ").plus(getString(R.string.metres))
        binding.txtViewFeelsLikeTemp.text = if (it.getCurrent().feelsLike.toInt() % 100 >= 50) {
            "${it.getCurrent().feelsLike.toInt().plus(1)}"
        } else {
            "${it.getCurrent().feelsLike.toInt()}"
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
        viewModel.currentLocation.observe(viewLifecycleOwner, Observer {
            binding.txtViewGovernorate.text = it[0]
            binding.txtViewLocality.text = it[1]
        })
    }

    private fun setCurrentTempAndWindSpeedDiscrimination() {
        viewModel.getCurrentTempMeasurementUnit(myView.context)
        viewModel.currentTempMeasurementUnit.observe(viewLifecycleOwner, Observer {
            when {
                it.isNullOrBlank() -> {
                    binding.txtViewCurrentTempDiscrimination.text = getString(R.string.temp_kelvin)
                    binding.txtViewFeelsLikeDiscrimination.text = getString(R.string.temp_kelvin)
                    binding.txtViewWindSpeedDiscrimination.text = getString(R.string.m_p_s)
                }
                it.equals("metric") -> {
                    binding.txtViewCurrentTempDiscrimination.text = getString(R.string.temp_celsius)
                    binding.txtViewFeelsLikeDiscrimination.text = getString(R.string.temp_celsius)
                    binding.txtViewWindSpeedDiscrimination.text = getString(R.string.m_p_s)
                }
                it.equals("imperial") -> {
                    binding.txtViewCurrentTempDiscrimination.text = getString(R.string.temp_fahrenheit)
                    binding.txtViewFeelsLikeDiscrimination.text = getString(R.string.temp_fahrenheit)
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
        val radioGroup = dialog.findViewById<RadioGroup>(R.id.radioGroup)
        val notificationSwitch = dialog.findViewById<SwitchCompat>(R.id.notificationSwitch)
        val btnOk = dialog.findViewById<Button>(R.id.btnOk)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val radioBtn = dialog.findViewById<RadioButton>(checkedId)
            when (checkedId) {
                R.id.radioBtnGPS -> {
                    // check permission
                    Log.d("asdfg:A", radioBtn.text.toString())
                    saveUpdateLocation()
                }
                R.id.radioBtnMap -> {
                    // play with map
                    Log.d("asdfg:B", radioBtn.text.toString())
                }
            }
        }

        btnOk.setOnClickListener {
            val checkedRadioButtonId = radioGroup.checkedRadioButtonId
            val radioBtn = dialog.findViewById<RadioButton>(checkedRadioButtonId)

            if (radioBtn == null || radioBtn.text.equals(getString(R.string.gps))) {
                saveUpdateLocation()
            } else {
                // get from map
                Log.d("asdfg:F", radioBtn.text.toString())
            }

            viewModel.isNotificationChecked(myView.context, notificationSwitch.isChecked)

            if (isAllPermissionsGranted) {
                dismissDialogAndGetWeather()
            }
        }
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun saveUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(
                myView.context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                myView.context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            isAllPermissionsGranted = true
            if (LocationManagerCompat.isLocationEnabled(myView.context.getSystemService(Context.LOCATION_SERVICE) as LocationManager)) {
                viewModel.saveUpdateLocation(myView.context)
            } else {
                Toast.makeText(myView.context, R.string.open_gps, Toast.LENGTH_LONG).show()
            }
        } else {
            requestLocationPermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    private val requestLocationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                if (it.value) {
                    // Permission is granted. Continue the action or workflow in your app.
                    Log.d("asdfg:", "${it.key} granted")
                    isAllPermissionsGranted = true
                    dismissDialogAndGetWeather()
                } else {
                    isAllPermissionsGranted = false
                    AppConstants.showAlert(
                        myView.context,
                        R.string.warning,
                        "${it.key} ${getString(R.string.cancelled)} \n\n${getString(R.string.permission_required)}",
                        R.drawable.ic_warning
                    )
                }

                if (isAllPermissionsGranted) {
                    saveUpdateLocation()
                }
            }
        }

    private fun dismissDialogAndGetWeather() {
        getWeatherDataOverNetwork()
        dialog.dismiss()
        viewModel.firstTimeComplete(myView.context)
    }

    private fun getWeatherDataOverNetwork() {
        viewModel.observeOnSharedPref(myView.context)
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.unRegisterOnSharedPreferenceChangeListener()
    }
}