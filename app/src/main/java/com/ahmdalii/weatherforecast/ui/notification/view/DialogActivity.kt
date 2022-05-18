package com.ahmdalii.weatherforecast.ui.notification.view

import android.media.MediaPlayer
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.databinding.ActivityDialogBinding
import com.ahmdalii.weatherforecast.db.notification.ConcreteLocalSourceNotification
import com.ahmdalii.weatherforecast.db.weather.ConcreteLocalSource
import com.ahmdalii.weatherforecast.ui.notification.repo.NotificationRepo
import com.ahmdalii.weatherforecast.ui.notification.viewmodel.NotificationViewModel
import com.ahmdalii.weatherforecast.ui.notification.viewmodel.NotificationViewModelFactory
import com.ahmdalii.weatherforecast.utils.AppConstants.DESCRIPTION
import com.ahmdalii.weatherforecast.utils.AppConstants.ICON
import com.ahmdalii.weatherforecast.utils.AppConstants.getIcon

class DialogActivity: AppCompatActivity() {

    private lateinit var binding: ActivityDialogBinding

    private lateinit var notificationViewModelFactory: NotificationViewModelFactory
    private lateinit var viewModel: NotificationViewModel

    private lateinit var description: String
    private lateinit var icon: String

    private var mediaPlayerSong: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        description = intent.getStringExtra(DESCRIPTION).toString()
        icon = intent.getStringExtra(ICON).toString()

        //                              width                               height
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setFinishOnTouchOutside(false)

        gettingViewModelReady()
        handleUI()
    }

    private fun gettingViewModelReady() {
        notificationViewModelFactory = NotificationViewModelFactory(
            NotificationRepo.getInstance(ConcreteLocalSourceNotification(this), ConcreteLocalSource(this))
        )
        viewModel = ViewModelProvider(this, notificationViewModelFactory)[NotificationViewModel::class.java]
    }

    private fun handleUI() {
        binding.imageIcon.setImageResource(getIcon(icon))
        binding.textDescription.text = description
        binding.btnDismiss.setOnClickListener {
            mediaPlayerSong!!.stop()
            finish()
        }

        mediaPlayerSong = MediaPlayer.create(this, R.raw.thunder)
        mediaPlayerSong!!.isLooping = true
        mediaPlayerSong!!.start()
    }
}