package com.ahmdalii.weatherforecast.ui.setting.view

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.databinding.FragmentSettingsBinding
import com.ahmdalii.weatherforecast.ui.map.view.MapsActivity
import com.ahmdalii.weatherforecast.ui.setting.repo.SettingsRepo
import com.ahmdalii.weatherforecast.ui.setting.viewmodel.SettingsViewModel
import com.ahmdalii.weatherforecast.ui.setting.viewmodel.SettingsViewModelFactory
import com.ahmdalii.weatherforecast.utils.AppConstants
import com.ahmdalii.weatherforecast.utils.AppConstants.APPLICATION_LANGUAGE_AR
import com.ahmdalii.weatherforecast.utils.AppConstants.APPLICATION_LANGUAGE_EN
import com.ahmdalii.weatherforecast.utils.AppConstants.COMING_FROM
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_METHOD_GPS
import com.ahmdalii.weatherforecast.utils.AppConstants.LOCATION_METHOD_MAP
import com.ahmdalii.weatherforecast.utils.AppConstants.MEASUREMENT_UNIT_IMPERIAL
import com.ahmdalii.weatherforecast.utils.AppConstants.MEASUREMENT_UNIT_METRIC
import com.ahmdalii.weatherforecast.utils.AppConstants.MEASUREMENT_UNIT_STANDARD
import com.ahmdalii.weatherforecast.utils.AppConstants.SETTING_FRAGMENT
import com.ahmdalii.weatherforecast.utils.AppConstants.WIND_SPEED_UNIT_M_P_H
import com.ahmdalii.weatherforecast.utils.AppConstants.WIND_SPEED_UNIT_M_P_S
import com.ahmdalii.weatherforecast.utils.AppConstants.atNight
import com.ahmdalii.weatherforecast.utils.AppConstants.setAppLocale
import com.ahmdalii.weatherforecast.utils.AppConstants.showBannerAd
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import java.util.*

class SettingsFragment : Fragment() {

    private lateinit var settingsViewModelFactory: SettingsViewModelFactory
    private lateinit var viewModel: SettingsViewModel
    private var _binding: FragmentSettingsBinding? = null

    private lateinit var myView: View
    private var isAllPermissionsGranted: Boolean = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.myView = view

        showBannerAd(binding.adView)
        gettingViewModelReady()
        handleUIEvents()
    }

    private fun handleUIEvents() {
        binding.radioGroupLocation.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioBtnGPS -> {
                    saveUpdateGPSLocation()
                }
                R.id.radioBtnMap -> {
                    // play with map
//                    binding.radioBtnMap.performClick()
                }
            }
        }
        binding.radioBtnMap.setOnClickListener {
            if (isServiceAvailable()) {
                viewModel.setLocationMethod(myView.context, LOCATION_METHOD_MAP)
                val intent = Intent(myView.context, MapsActivity::class.java)
                intent.putExtra(COMING_FROM, SETTING_FRAGMENT)
                startActivity(intent)
            }
        }

        binding.radioGroupLanguage.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioBtnEnglish -> {
                    setAppLocale(myView.context, APPLICATION_LANGUAGE_EN)
                    viewModel.setLanguage(myView.context, APPLICATION_LANGUAGE_EN)
                }
                R.id.radioBtnArabic -> {
                    setAppLocale(myView.context, APPLICATION_LANGUAGE_AR)
                    viewModel.setLanguage(myView.context, APPLICATION_LANGUAGE_AR)
                }
            }
        }

        binding.radioGroupWindSpeed.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioBtnMPS -> {
                    viewModel.setWindSpeedUnit(myView.context, WIND_SPEED_UNIT_M_P_S)
                }
                R.id.radioBtnMPH -> {
                    viewModel.setWindSpeedUnit(myView.context, WIND_SPEED_UNIT_M_P_H)
                }
            }
        }

        binding.radioGroupTemperature.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioBtnCelsius -> {
                    viewModel.setCurrentTempMeasurementUnit(myView.context, MEASUREMENT_UNIT_METRIC)
                }
                R.id.radioBtnKelvin -> {
                    viewModel.setCurrentTempMeasurementUnit(myView.context, MEASUREMENT_UNIT_STANDARD)
                }
                R.id.radioBtnFahrenheit -> {
                    viewModel.setCurrentTempMeasurementUnit(myView.context, MEASUREMENT_UNIT_IMPERIAL)
                }
            }
        }

        binding.radioGroupNotifications.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioBtnEnable -> {
                    viewModel.setNotificationChecked(myView.context, true)
                }
                R.id.radioBtnDisable -> {
                    viewModel.setNotificationChecked(myView.context, false)
                }
            }
        }

        binding.radioBtnEnglish.setOnClickListener {
            changeConfiguration(APPLICATION_LANGUAGE_EN)
        }

        binding.radioBtnArabic.setOnClickListener {
            changeConfiguration(APPLICATION_LANGUAGE_AR)
        }
    }

    private fun changeConfiguration(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val res: Resources = context!!.resources
        val config = Configuration(res.configuration)
        config.locale = locale
        res.updateConfiguration(config, res.displayMetrics)

        activity?.finish()
        startActivity(activity?.intent)
    }

    private fun saveUpdateGPSLocation() {
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
                viewModel.setLocationMethod(myView.context, LOCATION_METHOD_GPS)
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
                    isAllPermissionsGranted = true
                    viewModel.setLocationMethod(myView.context, LOCATION_METHOD_GPS)
                    binding.radioBtnGPS.isChecked = true
                } else {
                    isAllPermissionsGranted = false
                    binding.radioBtnMap.isChecked = true
                    AppConstants.showAlert(
                        myView.context,
                        R.string.warning,
                        "${it.key} ${getString(R.string.cancelled)} \n\n${getString(R.string.permission_required)}",
                        R.drawable.ic_warning
                    )
                }

                if (isAllPermissionsGranted) {
                    saveUpdateGPSLocation()
                }
            }
        }

    private fun gettingViewModelReady() {
        settingsViewModelFactory = SettingsViewModelFactory(
            SettingsRepo.getInstance()
        )
        viewModel = ViewModelProvider(this, settingsViewModelFactory)[SettingsViewModel::class.java]
        viewModel.observeOnSharedPref(myView.context)
        viewModel.getLocationMethod(myView.context)
        viewModel.getLanguage(myView.context)
        viewModel.getWindSpeedUnit(myView.context)
        viewModel.getCurrentTempMeasurementUnit(myView.context)
        viewModel.getNotificationChecked(myView.context)

        viewModel.errorMsgResponse.observe(viewLifecycleOwner) {
            Toast.makeText(myView.context, it, Toast.LENGTH_LONG).show()
        }
        /*viewModel.changedSuccessfully.observe(viewLifecycleOwner, {
            Toast.makeText(myView.context, it, Toast.LENGTH_SHORT).show()
        })*/

        viewModel.locationMethod.observe(viewLifecycleOwner) {
            if (it == LOCATION_METHOD_GPS) {
                binding.radioBtnGPS.isChecked = true
            } else {
                binding.radioBtnMap.isChecked = true
            }
        }
        viewModel.language.observe(viewLifecycleOwner) {
            if (it == APPLICATION_LANGUAGE_EN) {
                binding.radioBtnEnglish.isChecked = true
            } else {
                binding.radioBtnArabic.isChecked = true
            }
        }
        viewModel.windSpeedUnit.observe(viewLifecycleOwner) {
            if (it == WIND_SPEED_UNIT_M_P_S) {
                binding.radioBtnMPS.isChecked = true
            } else {
                binding.radioBtnMPH.isChecked = true
            }
        }
        viewModel.currentTempMeasurementUnit.observe(viewLifecycleOwner) {
            when (it) {
                MEASUREMENT_UNIT_METRIC -> {
                    binding.radioBtnCelsius.isChecked = true
                }
                MEASUREMENT_UNIT_STANDARD -> {
                    binding.radioBtnKelvin.isChecked = true
                }
                else -> {
                    binding.radioBtnFahrenheit.isChecked = true
                }
            }
        }
        viewModel.notificationChecked.observe(viewLifecycleOwner) {
            if (it) {
                binding.radioBtnEnable.isChecked = true
            } else {
                binding.radioBtnDisable.isChecked = true
            }
        }

        if (atNight) {
            binding.parentView.setBackgroundResource(R.drawable.background_image)
        } else {
            binding.parentView.setBackgroundResource(R.drawable.background_image_day)
        }
    }

    private fun isServiceAvailable(): Boolean {

        var isAvailable = false
        val googleAPIInstance = GoogleApiAvailability.getInstance()
        val googlePlayServicesAvailable =
            googleAPIInstance.isGooglePlayServicesAvailable(myView.context)

        when {
            googlePlayServicesAvailable == ConnectionResult.SUCCESS -> {
                isAvailable = true
            }
            googleAPIInstance.isUserResolvableError(googlePlayServicesAvailable) -> {
                val errorDialog =
                    googleAPIInstance.getErrorDialog(this, googlePlayServicesAvailable, 9001)
                errorDialog?.show()
            }
            else -> {
                Toast.makeText(myView.context, R.string.denied_map_request, Toast.LENGTH_SHORT).show()
            }
        }

        return isAvailable
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.unRegisterOnSharedPreferenceChangeListener()
    }
}