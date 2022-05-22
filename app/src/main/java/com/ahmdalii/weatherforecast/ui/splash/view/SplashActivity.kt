package com.ahmdalii.weatherforecast.ui.splash.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ahmdalii.weatherforecast.databinding.ActivitySplashBinding
import com.ahmdalii.weatherforecast.ui.HomeActivity
import com.ahmdalii.weatherforecast.ui.splash.repo.SplashRepo
import com.ahmdalii.weatherforecast.ui.splash.viewmodel.SplashViewModel
import com.ahmdalii.weatherforecast.ui.splash.viewmodel.SplashViewModelFactory
import com.ahmdalii.weatherforecast.utils.AppConstants.SPLASH_TIME_OUT
import com.ahmdalii.weatherforecast.utils.AppConstants.setAppLocale
import com.google.android.gms.ads.MobileAds

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    private lateinit var splashViewModelFactory: SplashViewModelFactory
    private lateinit var viewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        MobileAds.initialize(this) {}

        gettingViewModelReady()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }, SPLASH_TIME_OUT)
    }

    private fun gettingViewModelReady() {
        splashViewModelFactory = SplashViewModelFactory(
            SplashRepo.getInstance()
        )
        viewModel = ViewModelProvider(this, splashViewModelFactory)[SplashViewModel::class.java]
        viewModel.getLanguage(this)
        viewModel.language.observe(this) {
            setAppLocale(this, it)
        }
    }
}