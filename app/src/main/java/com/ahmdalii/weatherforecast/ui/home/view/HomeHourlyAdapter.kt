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
import com.ahmdalii.weatherforecast.model.Hourly
import com.ahmdalii.weatherforecast.ui.home.viewmodel.HomeViewModel
import com.ahmdalii.weatherforecast.utils.AppConstants
import com.ahmdalii.weatherforecast.utils.AppConstants.getDateTime
import com.ahmdalii.weatherforecast.utils.AppConstants.playAnimation
import com.bumptech.glide.Glide

class HomeHourlyAdapter(
    var context: Context,
    private var hourlyListWeather: List<Hourly>,
    var viewModel: HomeViewModel,
    private var viewLifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<HomeHourlyAdapter.ViewHolder>() {

//    var lastRowPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.row_hourly, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        playAnimation(holder.itemView, context, R.anim.row_slide_down)

        holder.txtViewHourlyTime?.text = getDateTime(hourlyListWeather[position].dt, "hh:mm a", viewModel.getLanguage(context))
        holder.imgViewHourlyWeatherIcon?.let {
            Glide
                .with(context)
                .load("${AppConstants.IMG_URL}${hourlyListWeather[position].weather[0].icon}@4x.png")
                .into(it)
        }
        viewModel.currentTempMeasurementUnit.observe(viewLifecycleOwner, {
            when {
                it.isNullOrBlank() -> {
                    holder.txtViewHourlyTempDiscrimination?.text = context.getString(R.string.temp_kelvin)
                }
                it.equals("metric") -> {
                    holder.txtViewHourlyTempDiscrimination?.text = context.getString(R.string.temp_celsius)
                }
                it.equals("imperial") -> {
                    holder.txtViewHourlyTempDiscrimination?.text = context.getString(R.string.temp_fahrenheit)
                }
            }
        })
        if (hourlyListWeather[position].temp.rem(100) >= 50) {
            holder.txtViewHourlyTemp?.text = "${hourlyListWeather[position].temp.toInt().plus(1)}"
        } else {
            holder.txtViewHourlyTemp?.text = "${hourlyListWeather[position].temp.toInt()}"
        }
    }

    override fun getItemCount(): Int {
        return hourlyListWeather.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDataToAdapter(hourlyListWeather: List<Hourly>) {
        this.hourlyListWeather = hourlyListWeather
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgViewHourlyWeatherIcon: ImageView? = null
            get() {
                if (field == null) {
                    field = itemView.findViewById(R.id.imgViewHourlyWeatherIcon)
                }
                return field
            }
            private set
        var txtViewHourlyTime: TextView? = null
            get() {
                if (field == null) {
                    field = itemView.findViewById(R.id.txtViewHourlyTime)
                }
                return field
            }
            private set
        var txtViewHourlyTemp: TextView? = null
            get() {
                if (field == null) {
                    field = itemView.findViewById(R.id.txtViewHourlyTemp)
                }
                return field
            }
            private set
        var txtViewHourlyTempDiscrimination: TextView? = null
            get() {
                if (field == null) {
                    field = itemView.findViewById(R.id.txtViewHourlyTempDiscrimination)
                }
                return field
            }
            private set
    }
}