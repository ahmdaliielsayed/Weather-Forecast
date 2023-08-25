package com.ahmdalii.weatherforecast.ui.home.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.model.Daily
import com.ahmdalii.weatherforecast.ui.home.viewmodel.HomeViewModel
import com.ahmdalii.weatherforecast.utils.AppConstants.MEASUREMENT_UNIT_IMPERIAL
import com.ahmdalii.weatherforecast.utils.AppConstants.MEASUREMENT_UNIT_METRIC
import com.ahmdalii.weatherforecast.utils.AppConstants.getDateTime
import com.ahmdalii.weatherforecast.utils.AppConstants.getIcon
import com.ahmdalii.weatherforecast.utils.AppConstants.playAnimation
import com.bumptech.glide.Glide

class HomeDailyAdapter(
    var context: Context,
    private var dailyListWeather: List<Daily>,
    var viewModel: HomeViewModel,
    private var viewLifecycleOwner: LifecycleOwner,
) : RecyclerView.Adapter<HomeDailyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.row_daily, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        playAnimation(holder.itemView, context, R.anim.row_slide_in)

        holder.txtViewDate?.text = getDateTime(dailyListWeather[position].dt, "EEE, MMM d", viewModel.getLanguage(context))
        holder.imgViewDayIcon?.let {
            Glide
                .with(context)
//                .load("${AppConstants.IMG_URL}${dailyListWeather[position].weather[0].icon}@4x.png")
                .load(getIcon(dailyListWeather[position].weather[0].icon))
                .into(it)
        }
        holder.txtViewDailyWeatherDescription?.text = dailyListWeather[position].weather[0].description
        viewModel.currentTempMeasurementUnit.observe(viewLifecycleOwner) {
            when {
                it.isNullOrBlank() -> {
                    holder.txtViewMinMaxDiscrimination?.text =
                        context.getString(R.string.temp_kelvin)
                }
                it.equals(MEASUREMENT_UNIT_METRIC) -> {
                    holder.txtViewMinMaxDiscrimination?.text =
                        context.getString(R.string.temp_celsius)
                }
                it.equals(MEASUREMENT_UNIT_IMPERIAL) -> {
                    holder.txtViewMinMaxDiscrimination?.text =
                        context.getString(R.string.temp_fahrenheit)
                }
            }
        }
        val minMaxTemp = StringBuilder()
        if (dailyListWeather[position].temp.max.rem(100) >= 50) {
            minMaxTemp.append(dailyListWeather[position].temp.max.toInt().plus(1)).append(" / ")
        } else {
            minMaxTemp.append(dailyListWeather[position].temp.max.toInt()).append(" / ")
        }
        if (dailyListWeather[position].temp.min.rem(100) >= 50) {
            minMaxTemp.append(dailyListWeather[position].temp.min.toInt().plus(1))
        } else {
            minMaxTemp.append(dailyListWeather[position].temp.min.toInt())
        }
        holder.txtViewDailyTempMinMax?.text = minMaxTemp
    }

    override fun getItemCount(): Int {
        return dailyListWeather.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDataToAdapter(dailyListWeather: List<Daily>) {
        this.dailyListWeather = dailyListWeather
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgViewDayIcon: ImageView? = null
            get() {
                if (field == null) {
                    field = itemView.findViewById(R.id.imgViewDayIcon)
                }
                return field
            }
            private set
        var txtViewDate: TextView? = null
            get() {
                if (field == null) {
                    field = itemView.findViewById(R.id.txtViewDate)
                }
                return field
            }
            private set
        var txtViewDailyWeatherDescription: TextView? = null
            get() {
                if (field == null) {
                    field = itemView.findViewById(R.id.txtViewDailyWeatherDescription)
                }
                return field
            }
            private set
        var txtViewDailyTempMinMax: TextView? = null
            get() {
                if (field == null) {
                    field = itemView.findViewById(R.id.txtViewDailyTempMinMax)
                }
                return field
            }
            private set
        var txtViewMinMaxDiscrimination: TextView? = null
            get() {
                if (field == null) {
                    field = itemView.findViewById(R.id.txtViewMinMaxDiscrimination)
                }
                return field
            }
            private set
    }
}
