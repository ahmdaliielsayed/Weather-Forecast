package com.ahmdalii.weatherforecast.ui.map.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.databinding.ActivityMapsBinding
import com.ahmdalii.weatherforecast.db.favorite.ConcreteLocalSourceFavorite
import com.ahmdalii.weatherforecast.model.FavoritePlace
import com.ahmdalii.weatherforecast.ui.map.repo.MapRepo
import com.ahmdalii.weatherforecast.ui.map.viewmodel.MapViewModel
import com.ahmdalii.weatherforecast.ui.map.viewmodel.MapViewModelFactory
import com.ahmdalii.weatherforecast.utils.AppConstants.COMING_FROM
import com.ahmdalii.weatherforecast.utils.AppConstants.FAVORITE_FRAGMENT
import com.ahmdalii.weatherforecast.utils.AppConstants.INITIAL_DIALOG
import com.ahmdalii.weatherforecast.utils.AppConstants.REPLY_INTENT_KEY
import com.ahmdalii.weatherforecast.utils.AppConstants.SETTING_FRAGMENT
import com.ahmdalii.weatherforecast.utils.AppConstants.getPlaceName
import com.ahmdalii.weatherforecast.utils.AppConstants.playAnimation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
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
    private lateinit var address: Address
    private lateinit var latLng: LatLng
    private var addMarker: Marker? = null
    private lateinit var comingFrom: String

    private var isViewOpen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        comingFrom = intent.getStringExtra(COMING_FROM).toString()

        gettingViewModelReady()
        // Prompt the user for permission.
        getLocationPermission()
        initializeMap()
        handleUIAction()
    }

    /*private val handler = Handler()
    private val postToServerRunnable = Runnable {
        searchForPlace(binding.txtInputEditTextSearchPlace.text.toString())
    }*/

    private fun handleUIAction() {
        /*binding.txtInputEditTextSearchPlace.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(value: CharSequence, start: Int, count: Int, after: Int) {
                handler.removeCallbacks(postToServerRunnable)
                if (value.isEmpty()) {
                    Toast.makeText(this@MapsActivity, R.string.empty_string, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    handler.postDelayed(postToServerRunnable, 3000)
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })*/

        binding.txtInputEditTextSearchPlace.setOnEditorActionListener { value, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH ||
                event.action == KeyEvent.ACTION_DOWN ||
                event.action == KeyEvent.KEYCODE_ENTER ){
                if (binding.txtInputEditTextSearchPlace.text.toString().isEmpty()) {
                    Toast.makeText(this@MapsActivity, R.string.empty_string, Toast.LENGTH_SHORT)
                        .show()
                } else {
                    searchForPlace(binding.txtInputEditTextSearchPlace.text.toString())
                }
                true
            } else {
                false
            }
        }

        binding.cardGPS.setOnClickListener {
            viewModel.getDeviceLocation(this)
        }

        binding.imgViewClose.setOnClickListener {
            playAnimation(binding.constraintLayout, this, R.anim.close_view_zoom_out_fade_out)
            binding.constraintLayout.visibility = View.GONE
            isViewOpen = false
        }
    }

    private fun searchForPlace(placeName: String) {
        viewModel.getCurrentAddress(this, placeName)
//        handler.removeCallbacks(postToServerRunnable)
    }

    private fun gettingViewModelReady() {
        mapViewModelFactory = MapViewModelFactory (
            MapRepo.getInstance(ConcreteLocalSourceFavorite(this))
        )
        viewModel = ViewModelProvider(this, mapViewModelFactory)[MapViewModel::class.java]
        viewModel.observeOnSharedPref(this)
        viewModel.getDeviceLocation(this)

        viewModel.currentDeviceLocation.observe(this, {
            latLng = LatLng(it.latitude, it.longitude)
            moveCamera(latLng)
            if (comingFrom == INITIAL_DIALOG) {
                val placeName = getPlaceName(this, latLng.latitude, latLng.longitude)
                openViewWithAddress(placeName, latLng)
            }
        })
        viewModel.searchAddress.observe(this, {
            address = it
            address.adminArea = it.adminArea
            address.locality = it.featureName
            Log.d("asdsad:", address.adminArea)
            Log.d("asdsad:", address.locality)
            Log.d("asdsad:", address.latitude.toString())
            Log.d("asdsad:", address.longitude.toString())
            latLng = LatLng(address.latitude, address.longitude)
            drawMarker(latLng,
                listOf(
                    address.adminArea ?: getString(R.string.unknown_adminArea),
                    address.locality ?: getString(R.string.unknown_locality)
                )
            )
            moveCamera(latLng)
            openViewWithAddress(address, latLng)
        })
    }

    private fun moveCamera(latLng: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_VALUE))
    }

    private fun drawMarker(latLng: LatLng, areaLocality: List<String>) {
        mMap.clear()
        addMarker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title(areaLocality[0].plus(", ").plus(areaLocality[1]))
        )
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

        handleOnMapClick()
    }

    private fun handleOnMapClick() {
        mMap.setOnMapClickListener {
            latLng = it
            address = getPlaceName(this, it.latitude, it.longitude)
            drawMarker(
                LatLng(latLng.latitude, latLng.longitude),
                listOf(
                    address.adminArea ?: getString(R.string.unknown_adminArea),
                    address.locality ?: getString(R.string.unknown_locality)
                )
            )

            openViewWithAddress(address, latLng)
        }
    }

    private fun openViewWithAddress(address: Address, latLng: LatLng) {
        binding.txtViewAddressGovernorateValue.text = address.adminArea ?: getString(R.string.unknown_adminArea)
        binding.txtViewAddressLocalityValue.text = address.locality ?: getString(R.string.unknown_locality)
        if (!isViewOpen) {
            binding.constraintLayout.visibility = View.VISIBLE
            playAnimation(binding.constraintLayout, this, R.anim.open_view_zoom_in_fade_in)
            isViewOpen = true
        }

        binding.btnSave.setOnClickListener {
            when (comingFrom) {
                INITIAL_DIALOG -> {
                    viewModel.saveUpdateLocationPlace(this, latLng, address)
                }
                FAVORITE_FRAGMENT -> {
                    val replyIntent = Intent()
                    replyIntent.putExtra(REPLY_INTENT_KEY,
                        FavoritePlace(latLng.latitude, latLng.longitude,
                            address.adminArea ?: getString(R.string.unknown_adminArea),
                            address.locality ?: getString(R.string.unknown_locality)))
                    setResult(RESULT_OK, replyIntent)
                }
                SETTING_FRAGMENT -> {
                    viewModel.saveUpdateLocationPlace(this, latLng, address)
                }
            }
            finish()
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
                    locationPermissionGranted = true
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