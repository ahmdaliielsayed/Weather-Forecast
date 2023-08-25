package com.ahmdalii.weatherforecast.ui.notification.view

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.databinding.FragmentNotificationsBinding
import com.ahmdalii.weatherforecast.db.notification.ConcreteLocalSourceNotification
import com.ahmdalii.weatherforecast.db.weather.ConcreteLocalSource
import com.ahmdalii.weatherforecast.model.MyAlert
import com.ahmdalii.weatherforecast.model.WeatherModel
import com.ahmdalii.weatherforecast.ui.notification.repo.NotificationRepo
import com.ahmdalii.weatherforecast.ui.notification.viewmodel.NotificationViewModel
import com.ahmdalii.weatherforecast.ui.notification.viewmodel.NotificationViewModelFactory
import com.ahmdalii.weatherforecast.utils.AppConstants
import com.ahmdalii.weatherforecast.utils.AppConstants.ALARM_CHECKED
import com.ahmdalii.weatherforecast.utils.AppConstants.APPLICATION_LANGUAGE_AR
import com.ahmdalii.weatherforecast.utils.AppConstants.APPLICATION_LANGUAGE_EN
import com.ahmdalii.weatherforecast.utils.AppConstants.NOTIFICATION_CHECKED
import com.ahmdalii.weatherforecast.utils.AppConstants.atNight
import com.ahmdalii.weatherforecast.utils.AppConstants.getDateTime
import com.ahmdalii.weatherforecast.utils.AppConstants.showBannerAd
import com.ahmdalii.weatherforecast.utils.AppSharedPref
import com.ahmdalii.weatherforecast.utils.WorkRequestManager.createWorkRequest
import com.ahmdalii.weatherforecast.utils.WorkRequestManager.removeWork
import java.util.*

class NotificationFragment : Fragment(), OnAlertClickListener {

    private lateinit var notificationViewModelFactory: NotificationViewModelFactory
    private lateinit var viewModel: NotificationViewModel
    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var myView: View
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var linearNotificationLayoutManager: LinearLayoutManager
    private lateinit var dialog: Dialog
    private lateinit var fromTime: TextView
    private lateinit var fromDate: TextView
    private var fromDateString: String? = null
    private var fromTimeInMillis: Long = 0
    private lateinit var toTime: TextView
    private lateinit var toDate: TextView
    private var toDateString: String? = null
    private var toTimeInMillis: Long = 0
    private lateinit var radioBtnAlarm: RadioButton
    private lateinit var radioBtnNotification: RadioButton

    private lateinit var weatherModel: WeatherModel

    private lateinit var language: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.myView = view

        showBannerAd(binding.adView)
        gettingViewModelReady()
        initRecyclerView()
        handleUIEvents()
    }

    private fun gettingViewModelReady() {
        notificationViewModelFactory = NotificationViewModelFactory(
            NotificationRepo.getInstance(ConcreteLocalSourceNotification(myView.context), ConcreteLocalSource(myView.context)),
        )
        viewModel = ViewModelProvider(this, notificationViewModelFactory)[NotificationViewModel::class.java]

        language = if (viewModel.getLanguage(myView.context) == APPLICATION_LANGUAGE_EN) {
            APPLICATION_LANGUAGE_EN
        } else {
            APPLICATION_LANGUAGE_AR
        }
    }

    private fun initRecyclerView() {
        notificationAdapter = NotificationAdapter(myView.context, emptyList(), language, this)
        linearNotificationLayoutManager = LinearLayoutManager(myView.context, RecyclerView.VERTICAL, false)
        binding.recyclerView.apply {
            adapter = notificationAdapter
            layoutManager = linearNotificationLayoutManager
        }

        viewModel.getAlertList().observe(viewLifecycleOwner) {
            if (it == null || it.isEmpty()) {
                binding.noAlertData.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                notificationAdapter.setDataToAdapter(it)
                binding.noAlertData.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }

        viewModel.id.observe(viewLifecycleOwner) { id ->
            viewModel.getAllStoredWeatherModel(myView.context).observe(viewLifecycleOwner) { weatherModel ->
                if (weatherModel != null) {
                    this.weatherModel = weatherModel
                    viewModel.getAlert(id)
                }
            }
        }
        viewModel.myAlert.observe(viewLifecycleOwner) { alert ->
            if (weatherModel.alerts.isNullOrEmpty()) {
                setOneTimeWorkRequest(
                    alert,
//                    getString(R.string.weather_description),
                    weatherModel.current.weather[0].description,
                    weatherModel.current.weather[0].icon,
                )
            } else {
                setOneTimeWorkRequest(
                    alert,
                    weatherModel.alerts!![0].tags[0],
                    weatherModel.current.weather[0].icon,
                )
            }
        }

        if (atNight) {
            binding.parentView.setBackgroundResource(R.drawable.background_image)
        } else {
            binding.parentView.setBackgroundResource(R.drawable.background_image_day)
        }
    }

    private fun setOneTimeWorkRequest(alert: MyAlert, description: String, icon: String) {
        createWorkRequest(alert, description, icon, myView.context, fromTimeInMillis)
    }

    private fun handleUIEvents() {
        binding.fabAddAlert.setOnClickListener {
            val pm = view?.context?.getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!Settings.canDrawOverlays(view?.context)) {
                askForDrawOverlaysPermission()
            } else if (!pm.isIgnoringBatteryOptimizations(myView.context.packageName)) {
                val intent = Intent()
                intent.action = ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:" + view?.context?.packageName)
                startActivity(intent)
            } else {
                showAddAlertDialog()
            }
        }
    }

    private fun askForDrawOverlaysPermission() {
        if (!Settings.canDrawOverlays(view?.context)) {
            if ("xiaomi" == Build.MANUFACTURER.lowercase(Locale.ROOT)) {
                val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
                intent.setClassName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity",
                )
                intent.putExtra("extra_pkgname", view?.context?.packageName)
                AlertDialog.Builder(view!!.context)
                    .setTitle(R.string.draw_overlays)
                    .setMessage(R.string.draw_overlays_description)
                    .setPositiveButton(R.string.go_to_settings) { _, _ ->
                        startActivity(
                            intent,
                        )
                    }
                    .setIcon(R.drawable.ic_warning)
                    .show()
            } else {
                AlertDialog.Builder(view!!.context)
                    .setTitle(R.string.warning)
                    .setMessage(R.string.error_msg_permission_required)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        val permissionIntent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + view?.context?.packageName),
                        )
                        runtimePermissionResultLauncher.launch(permissionIntent)
                    }
                    .setIcon(R.drawable.ic_warning)
                    .show()
            }
        }
    }

    private val runtimePermissionResultLauncher = registerForActivityResult(StartActivityForResult()) { }

    private fun showAddAlertDialog() {
        dialog = Dialog(myView.context)
        dialog.setContentView(R.layout.alert_notification_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val linearCardFrom = dialog.findViewById<CardView>(R.id.linearCardFrom)
        fromTime = dialog.findViewById(R.id.txtViewFromTime)
        fromDate = dialog.findViewById(R.id.txtViewFromDate)
        val linearCardTo = dialog.findViewById<CardView>(R.id.linearCardTo)
        toTime = dialog.findViewById(R.id.txtViewToTime)
        toDate = dialog.findViewById(R.id.txtViewToDate)
        radioBtnAlarm = dialog.findViewById(R.id.radioBtnAlarm)
        radioBtnNotification = dialog.findViewById(R.id.radioBtnNotification)
        val btnSave = dialog.findViewById<Button>(R.id.btnSave)

        radioBtnAlarm.setOnClickListener {
            radioBtnNotification.isChecked = false
        }
        radioBtnNotification.setOnClickListener {
            radioBtnAlarm.isChecked = false
        }

        linearCardFrom.setOnClickListener {
            showDateTimePicker(true)
        }

        linearCardTo.setOnClickListener {
            showDateTimePicker(false)
        }

        btnSave.setOnClickListener {
            if (fromDateString.isNullOrBlank() || fromDateString.isNullOrEmpty()) {
                Toast.makeText(myView.context, R.string.empty_alarm_from, Toast.LENGTH_LONG).show()
            } else if (toDateString.isNullOrBlank() || toDateString.isNullOrEmpty()) {
                Toast.makeText(myView.context, R.string.empty_alarm_to, Toast.LENGTH_LONG).show()
            } else {
                val alarmOrNotification: String = if (radioBtnAlarm.isChecked) {
                    ALARM_CHECKED
                } else {
                    NOTIFICATION_CHECKED
                }
                viewModel.insertAlert(MyAlert(null, fromTimeInMillis, toTimeInMillis, alarmOrNotification))
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private lateinit var date: Calendar
    private fun showDateTimePicker(isFrom: Boolean) {
        Locale.setDefault(
            Locale(
                AppSharedPref.getInstance(myView.context, AppConstants.SETTING_FILE).getStringValue(
                    AppConstants.APPLICATION_LANGUAGE,
                    AppConstants.getDisplayCurrentLanguage(),
                ),
            ),
        )
        val currentDate = Calendar.getInstance()
        date = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            context!!,
            { _, year, monthOfYear, dayOfMonth ->
                date.set(year, monthOfYear, dayOfMonth)
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        date.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        date.set(Calendar.MINUTE, minute)

                        if (isFrom) {
                            fromTime.text = getDateTime(date, "hh:mm a", language)
                            fromDateString = getDateTime(date, "EEE, MMM d", language)
                            fromDate.text = fromDateString
                            fromTimeInMillis = date.timeInMillis
                        } else {
                            toTime.text = getDateTime(date, "hh:mm a", language)
                            toDateString = getDateTime(date, "EEE, MMM d", language)
                            toDate.text = toDateString
                            toTimeInMillis = date.timeInMillis
                        }
                    },
                    currentDate[Calendar.HOUR_OF_DAY],
                    currentDate[Calendar.MINUTE],
                    false,
                ).show()
            },
            currentDate[Calendar.YEAR],
            currentDate[Calendar.MONTH],
            currentDate[Calendar.DATE],
        )
        datePickerDialog.datePicker.minDate = currentDate.timeInMillis
        datePickerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRemoveClick(myAlert: MyAlert) {
        AlertDialog.Builder(myView.context)
            .setTitle(R.string.warning)
            .setMessage(getString(R.string.delete_place))
            .setPositiveButton(R.string.ok) { _, _ ->
                viewModel.deleteAlert(myAlert)
                removeWork(myAlert.id.toString(), myView.context)
            }
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .setIcon(R.drawable.ic_warning)
            .show()
    }

    override fun onAlertClick(myAlert: MyAlert) {
    }
}
