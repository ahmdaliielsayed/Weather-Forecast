package com.ahmdalii.weatherforecast.ui.home.view

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.databinding.FragmentHomeBinding
import com.ahmdalii.weatherforecast.network.WeatherClient
import com.ahmdalii.weatherforecast.ui.home.repo.HomeRepo
import com.ahmdalii.weatherforecast.ui.home.viewmodel.HomeViewModel
import com.ahmdalii.weatherforecast.ui.home.viewmodel.HomeViewModelFactory
import com.ahmdalii.weatherforecast.utils.AppConstants
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModelFactory: HomeViewModelFactory
    private lateinit var viewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    private lateinit var myView: View
    private lateinit var dialog: Dialog
    private var isAllPermissionsGranted: Boolean = false

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
        configureDialog()
    }

    private fun gettingViewModelReady() {
        homeViewModelFactory = HomeViewModelFactory(
            HomeRepo.getInstance(
                WeatherClient.getInstance()
            )
        )
        viewModel = ViewModelProvider(this, homeViewModelFactory)[HomeViewModel::class.java]
//        viewModel.getCurrentWeatherOverNetwork(myView.context)

        val textView: TextView = binding.textHome
        viewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        viewModel.weatherModelResponse.observe(viewLifecycleOwner, Observer {
            Log.d("asdfg:", it.toString())
        })
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
        viewModel.getCurrentWeatherOverNetwork(myView.context)
        dialog.dismiss()
    }
}