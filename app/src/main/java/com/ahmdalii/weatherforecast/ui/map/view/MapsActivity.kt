package com.ahmdalii.weatherforecast.ui.map.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.databinding.ActivityMapsBinding
import com.ahmdalii.weatherforecast.ui.map.repo.MapRepo
import com.ahmdalii.weatherforecast.ui.map.viewmodel.MapViewModel
import com.ahmdalii.weatherforecast.ui.map.viewmodel.MapViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var mapViewModelFactory: MapViewModelFactory
    private lateinit var viewModel: MapViewModel

    private var locationPermissionGranted: Boolean = false

    private val DEFAULT_ZOOM_VALUE = 15f

    private var markerList = mutableListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gettingViewModelReady()
        // Prompt the user for permission.
        getLocationPermission()
        initializeMap()
        handleUIAction()
    }

    private val handler = Handler()
    private val postToServerRunnable = Runnable {
        searchForPlace(binding.txtInputEditTextSearchPlace.text.toString())
    }
    private fun handleUIAction() {
        binding.txtInputEditTextSearchPlace.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(value: CharSequence, start: Int, count: Int, after: Int) {

                // remove existing callback (timer reset)
                handler.removeCallbacks(postToServerRunnable)

                if (value.isEmpty()) {
                    Log.d("asdfer:hand", "isEmpty")
                    Toast.makeText(this@MapsActivity, R.string.empty_string, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Log.d("asdfer:hand", value.toString())
                    // 500 millisecond delay. Change to whatever delay you want.
                    handler.postDelayed(postToServerRunnable, 3000)
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        binding.cardGPS.setOnClickListener {
            viewModel.getDeviceLocation(this)
        }
    }

    private fun searchForPlace(placeName: String) {
        Log.d("asdfer:pN", placeName)
        viewModel.getCurrentAddress(this, placeName)
    }

    private fun gettingViewModelReady() {
        mapViewModelFactory = MapViewModelFactory(
            MapRepo.getInstance()
        )
        viewModel = ViewModelProvider(this, mapViewModelFactory)[MapViewModel::class.java]
        viewModel.observeOnSharedPref(this)
        viewModel.getDeviceLocation(this)

        viewModel.currentDeviceLocation.observe(this, {
            Log.d("asdfer:cDL", it.toString())
            moveCamera(LatLng(it.latitude, it.longitude))
        })
        viewModel.searchAddress.observe(this, {
            Log.d("asdfer:Ob", it.toString())
            moveCameraAndAddMarker(LatLng(it.latitude, it.longitude), listOf(it.adminArea, it.locality))
        })
    }

    private fun moveCamera(latLng: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_VALUE))
    }

    private fun moveCameraAndAddMarker(latLng: LatLng, areaLocality: List<String>) {
        clearMarkers()
        drawMarker(latLng, areaLocality)
        moveCamera(latLng)
    }

    private fun drawMarker(latLng: LatLng, areaLocality: List<String>) {
        val addMarker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title(areaLocality[0].plus(", ").plus(areaLocality[1]))
        )

        markerList.add(addMarker!!)
    }

    private fun clearMarkers() {
        if (markerList.isNotEmpty()) {
            markerList[0].remove()
            markerList.clear()
        }
    }

    private fun initializeMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        mMap.setOnMapClickListener {
            Toast.makeText(this@MapsActivity, "Lat: ${it.latitude} \n Lon: ${it.latitude}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updateLocationUI() {
        try {
            mMap.uiSettings.isMyLocationButtonEnabled = false

            if (locationPermissionGranted) {
                // to view my current location
                mMap.isMyLocationEnabled = true
                // to view button that return to my current location
//                mMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap.isMyLocationEnabled = false
//                mMap.uiSettings.isMyLocationButtonEnabled = false
//                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun getLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            /*&& ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED*/
        ) {
            locationPermissionGranted = true
            /*if (LocationManagerCompat.isLocationEnabled(getSystemService(Context.LOCATION_SERVICE) as LocationManager)) {
                viewModel.saveUpdateLocation(this)
            } else {
                Toast.makeText(this, R.string.open_gps, Toast.LENGTH_LONG).show()
            }*/
        } else {
            getRequestLocationPermissions()
        }
    }

    private fun getRequestLocationPermissions() {
        requestLocationPermissions.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION
                /*,
                Manifest.permission.ACCESS_COARSE_LOCATION*/
            )
        )
    }

    private val requestLocationPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                if (it.value) {
                    // Permission is granted. Continue the action or workflow in your app.
                    Log.d("asdfg:", "${it.key} granted")
                    locationPermissionGranted = true
                    /*if (isNetworkConnected) {
                        dismissDialogAndGetWeather()
                    } else {
                        Toast.makeText(myView.context, R.string.first_time_fetch, Toast.LENGTH_LONG).show()
                    }*/
                    initializeMap()
                    viewModel.getDeviceLocation(this)
                } else {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.warning)
                        .setMessage("${it.key} ${getString(R.string.cancelled)} \n\n${getString(R.string.permission_required)}")
                        .setPositiveButton(R.string.ok) { _, _ ->
                            getRequestLocationPermissions()
                        }
                        .setIcon(R.drawable.ic_warning)
                        .show()
                }
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.unRegisterOnSharedPreferenceChangeListener()
    }
}