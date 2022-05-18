package com.ahmdalii.weatherforecast.ui.notification.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.model.MyAlert
import com.ahmdalii.weatherforecast.utils.AppConstants.getDateTime
import com.ahmdalii.weatherforecast.utils.AppConstants.playAnimation

class NotificationAdapter(
    var context: Context,
    private var alerts: List<MyAlert>,
    var language: String,
    private var onAlertClickListener: OnAlertClickListener
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    private var lastRowPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.row_alert, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.adapterPosition > lastRowPosition) {
            playAnimation(holder.itemView, context, R.anim.row_slide_in)

            holder.txtViewFromTime?.text = getDateTime(alerts[position].startDate, "hh:mm a", language)
            holder.txtViewFromDate?.text = getDateTime(alerts[position].startDate, "EEE, MMM d", language)
            holder.txtViewToTime?.text = getDateTime(alerts[position].endDate, "hh:mm a", language)
            holder.txtViewToDate?.text = getDateTime(alerts[position].endDate, "EEE, MMM d", language)

            holder.imgViewDeleteIcon?.setOnClickListener {
                onAlertClickListener.onRemoveClick(alerts[position])
            }

            holder.itemView.setOnClickListener {
                onAlertClickListener.onAlertClick(alerts[position])
            }

            lastRowPosition = holder.adapterPosition
        }
    }

    override fun getItemCount(): Int {
        return alerts.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDataToAdapter(alerts: List<MyAlert>) {
        this.alerts = alerts
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imgViewDeleteIcon: ImageView? = null
            get() {
                if (field == null) {
                    field = itemView.findViewById(R.id.imgViewDeleteIcon)
                }
                return field
            }
            private set
        var txtViewFromTime: TextView? = null
            get() {
                if (field == null) {
                    field = itemView.findViewById(R.id.txtViewFromTime)
                }
                return field
            }
            private set
        var txtViewFromDate: TextView? = null
            get() {
                if (field == null) {
                    field = itemView.findViewById(R.id.txtViewFromDate)
                }
                return field
            }
            private set
        var txtViewToTime: TextView? = null
            get() {
                if (field == null) {
                    field = itemView.findViewById(R.id.txtViewToTime)
                }
                return field
            }
            private set
        var txtViewToDate: TextView? = null
            get() {
                if (field == null) {
                    field = itemView.findViewById(R.id.txtViewToDate)
                }
                return field
            }
            private set
    }
}