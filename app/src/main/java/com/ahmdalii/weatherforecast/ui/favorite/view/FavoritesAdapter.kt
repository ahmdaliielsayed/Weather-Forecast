package com.ahmdalii.weatherforecast.ui.favorite.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.model.FavoritePlace
import com.ahmdalii.weatherforecast.utils.AppConstants.playAnimation

class FavoritesAdapter(
    var context: Context,
    var places: List<FavoritePlace>,
    private var onFavoriteClickListener: OnFavoriteClickListener
) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    var lastRowPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.row_favorite, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder.adapterPosition > lastRowPosition) {
            playAnimation(holder.itemView, context, R.anim.row_slide_in)

            holder.txtViewGovernorate?.text = places[position].adminArea
            holder.txtViewRegion?.text = places[position].locality

            holder.imgViewDeleteIcon?.setOnClickListener {
                onFavoriteClickListener.onRemoveClick(places[position])
            }

            holder.itemView.setOnClickListener {
                onFavoriteClickListener.onPlaceClick(places[position])
            }

            lastRowPosition = holder.adapterPosition
        }
    }

    override fun getItemCount(): Int {
        return places.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setDataToAdapter(places: List<FavoritePlace>) {
        this.places = places
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
        var txtViewGovernorate: TextView? = null
            get() {
                if (field == null) {
                    field = itemView.findViewById(R.id.txtViewGovernorate)
                }
                return field
            }
            private set
        var txtViewRegion: TextView? = null
            get() {
                if (field == null) {
                    field = itemView.findViewById(R.id.txtViewRegion)
                }
                return field
            }
            private set
    }
}