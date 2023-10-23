package com.dicoding.storyapp.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.dicoding.storyapp.R
import com.dicoding.storyapp.databinding.ActivityAddStoryBinding
import com.dicoding.storyapp.ui.activities.CameraActivity.Companion.CAMERAX_RESULT
import com.dicoding.storyapp.ui.viewmodels.AddViewModel
import com.dicoding.storyapp.ui.viewmodels.ViewModelFactory
import com.dicoding.storyapp.data.Result
import com.dicoding.storyapp.utils.reduceFileImage
import com.dicoding.storyapp.utils.uriToFile
import com.google.android.material.snackbar.Snackbar
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.io.File

class AddStoryActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentImageUri: Uri? = null
    private var location: Location? = null

    private val binding: ActivityAddStoryBinding by lazy {
        ActivityAddStoryBinding.inflate(layoutInflater)
    }

    private val viewModel by viewModels<AddViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private val requestCameraLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission request granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }

    private val requestLocationLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    // Precise location access granted.
                    getMyLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    // Only approximate location access granted.
                    getMyLocation()
                }
                else -> {
                    Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
                }
            }
        }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERAX_RESULT) {
            currentImageUri =
                it.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)?.toUri()
            currentImageUri?.let { uri ->
                binding.previewImageView.setImageURI(uri)
            }
        }
    }

    private val launchCamera = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            currentImageUri?.let {
                Log.d("Image URI", "showImage: $it")
                binding.previewImageView.setImageURI(it)
            }
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (!checkPermission(Manifest.permission.CAMERA)) {
            requestCameraLauncher.launch(Manifest.permission.CAMERA)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupView()
        setupAction()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {
        binding.camera.setOnClickListener {
            launcherGallery.launch(Intent(this, CameraActivity::class.java))
        }

        binding.galleryButton.setOnClickListener {
            launchCamera.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
                    !checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    requestLocationLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                } else if (location == null) {
                    getMyLocation()
                }
            }
        }

        binding.buttonAdd.setOnClickListener {
            currentImageUri?.let { uri ->
                val imageFile = uriToFile(uri, this).reduceFileImage()
                val desc = binding.edAddDescription.text.toString()

                if (binding.switchLocation.isChecked) {
                    getMyLocation()
                    if (location != null) {
                        addStory(desc, imageFile, location)
                    } else {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.empty_location_warning),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    addStory(desc, imageFile, null)
                }
            } ?: Snackbar.make(
                binding.root,
                getString(R.string.empty_image_warning),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun addStory(desc: String, imageFile: File, location: Location?) {
        viewModel.addStory(
            desc,
            imageFile,
            location?.latitude?.toFloat(),
            location?.longitude?.toFloat(),
        ).observe(this) {
            if (it != null) {
                when (it) {
                    is Result.Loading -> {
                        showLoading(true)
                    }

                    is Result.Success -> {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags =
                                Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        startActivity(intent)
                        finish()
                    }

                    is Result.Error -> {
                        Snackbar.make(binding.root, it.error, Snackbar.LENGTH_SHORT).show()
                        showLoading(false)
                    }
                }
            }
        }
    }

    private fun getMyLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        this.location = location
                    } else {
                        requestLocationUpdates()
                    }
                }
                .addOnFailureListener { e ->
                    Snackbar.make(
                        binding.root,
                        "Failed to fetch location: ${e.message}",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
        } else {
            if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
                !checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                requestLocationLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(10000)  // Update interval in milliseconds
            .setFastestInterval(5000)

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                val newLocation = p0.lastLocation
                this@AddStoryActivity.location = newLocation
            }
        }

        if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            !checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            requestLocationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.buttonAdd.isEnabled = !isLoading
        binding.buttonAdd.text = if (isLoading) {
            getString(R.string.loading)
        } else {
            getString(R.string.upload)
        }
    }
}