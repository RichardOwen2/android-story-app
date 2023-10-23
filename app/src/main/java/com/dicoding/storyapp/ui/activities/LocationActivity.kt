package com.dicoding.storyapp.ui.activities

import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import com.dicoding.storyapp.R

import com.dicoding.storyapp.data.Result
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.dicoding.storyapp.databinding.ActivityLocationBinding
import com.dicoding.storyapp.ui.viewmodels.LocationViewModel
import com.dicoding.storyapp.ui.viewmodels.ViewModelFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.snackbar.Snackbar

class LocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private val binding: ActivityLocationBinding by lazy {
        ActivityLocationBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<LocationViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        with(mMap.uiSettings) {
            isZoomControlsEnabled = true
            isIndoorLevelPickerEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }

        setMapStyle()
        addManyMarker()
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if (!success) {
                Snackbar.make(binding.root, "Style parsing failed.", Snackbar.LENGTH_SHORT).show()
            }
        } catch (exception: Resources.NotFoundException) {
            Snackbar.make(binding.root, "Can't find style. Error: $exception", Snackbar.LENGTH_SHORT).show()
        }
    }

    private val boundsBuilder = LatLngBounds.Builder()

    private fun addManyMarker() {
        viewModel.getStoriesWithLocation().observe(this) { storyResponse ->
            when (storyResponse) {
                is Result.Loading -> showLoading(true)
                is Result.Success -> {
                    showLoading(false)
                    storyResponse.data.listStory.forEach { story ->
                        val latLng = LatLng(story.lat!!.toDouble(), story.lon!!.toDouble())
                        mMap.addMarker(MarkerOptions().position(latLng).title("Story: ${story.name}"))
                        boundsBuilder.include(latLng)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    }
                }
                is Result.Error -> {
                    showLoading(false)
                    Snackbar.make(binding.root, storyResponse.error, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}