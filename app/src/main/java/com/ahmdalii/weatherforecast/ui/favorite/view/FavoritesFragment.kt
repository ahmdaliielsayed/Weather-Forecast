package com.ahmdalii.weatherforecast.ui.favorite.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ahmdalii.weatherforecast.R
import com.ahmdalii.weatherforecast.databinding.FragmentFavoriteBinding
import com.ahmdalii.weatherforecast.db.favorite.ConcreteLocalSourceFavorite
import com.ahmdalii.weatherforecast.model.FavoritePlace
import com.ahmdalii.weatherforecast.network.WeatherClient
import com.ahmdalii.weatherforecast.ui.favorite.repo.FavoriteRepo
import com.ahmdalii.weatherforecast.ui.favorite.viewmodel.FavoriteViewModel
import com.ahmdalii.weatherforecast.ui.favorite.viewmodel.FavoriteViewModelFactory
import com.ahmdalii.weatherforecast.ui.map.view.MapsActivity
import com.ahmdalii.weatherforecast.utils.AppConstants
import com.ahmdalii.weatherforecast.utils.AppConstants.COMING_FROM
import com.ahmdalii.weatherforecast.utils.AppConstants.FAVORITE_KEY
import com.ahmdalii.weatherforecast.utils.AppConstants.REPLY_INTENT_KEY
import com.ahmdalii.weatherforecast.utils.AppConstants.isInternetAvailable
import com.google.android.material.snackbar.Snackbar

class FavoritesFragment : Fragment(), OnFavoriteClickListener {

    private lateinit var favoritePlacesViewModelFactory: FavoriteViewModelFactory
    private lateinit var viewModel: FavoriteViewModel
    private var _binding: FragmentFavoriteBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var myView: View
    private lateinit var favoritesAdapter: FavoritesAdapter
    private lateinit var linearFavoriteLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.myView = view
        gettingViewModelReady()
        initRecyclerView()
        handleUIEvents()
    }

    private fun gettingViewModelReady() {
        favoritePlacesViewModelFactory = FavoriteViewModelFactory(
            FavoriteRepo.getInstance(WeatherClient.getInstance(), ConcreteLocalSourceFavorite(myView.context))
        )
        viewModel = ViewModelProvider(this, favoritePlacesViewModelFactory)[FavoriteViewModel::class.java]
    }

    private fun initRecyclerView() {
        favoritesAdapter = FavoritesAdapter(myView.context, emptyList(), this)
        linearFavoriteLayoutManager = LinearLayoutManager(myView.context, RecyclerView.VERTICAL, false)
        binding.recyclerView.apply {
            adapter = favoritesAdapter
            layoutManager = linearFavoriteLayoutManager
        }

        viewModel.getFavoritePlacesList().observe(this, {
            if (it == null || it.isEmpty()) {
                binding.noFavoriteData.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                favoritesAdapter.setDataToAdapter(it)
                binding.noFavoriteData.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        })
    }

    private fun handleUIEvents() {
        binding.fabAddPlace.setOnClickListener {
            val intent = Intent(myView.context, MapsActivity::class.java)
            intent.putExtra(COMING_FROM, AppConstants.FAVORITE_FRAGMENT)
            requestAddFavoritePlace.launch(intent)
        }
    }

    private val requestAddFavoritePlace =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val favoritePlace = data?.getParcelableExtra<FavoritePlace>(REPLY_INTENT_KEY)
                if (favoritePlace != null) {
                    viewModel.insertFavoritePlace(favoritePlace)
                }
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRemoveClick(favoritePlace: FavoritePlace) {
        // check with dialog then take action
        AlertDialog.Builder(myView.context)
            .setTitle(R.string.warning)
            .setMessage(getString(R.string.delete_place))
            .setPositiveButton(R.string.ok) { _, _ ->
                viewModel.deleteFavoritePlace(favoritePlace)
            }
            .setNegativeButton(R.string.cancel) {_, _ -> }
            .setIcon(R.drawable.ic_warning)
            .show()
    }

    override fun onPlaceClick(favoritePlace: FavoritePlace) {
        if (isInternetAvailable(myView.context)) {
            val intent = Intent(myView.context, FavoritePlaceViewActivity::class.java)
            intent.putExtra(FAVORITE_KEY, favoritePlace)
            startActivity(intent)
        } else {
            Snackbar.make(myView, getString(R.string.connection_lost), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }
}