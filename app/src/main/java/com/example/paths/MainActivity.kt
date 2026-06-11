package com.example.paths

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.paths.ui.theme.PathsTheme
import com.google.android.gms.location.*

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                      permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        
        // Find AuthViewModel and update it
        // We'll pass the authVM to the launcher callback indirectly or just let the UI recompose
        // In this case, I'll use a property to store a reference to authVM for the callback
        this.authVMReference?.setLocationPermissionGranted(granted)
    }

    private var authVMReference: AuthViewModel? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                authVMReference?.setUserLocation(location.latitude, location.longitude)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        enableEdgeToEdge()
        setContent {
            val authVM: AuthViewModel = viewModel()
            this.authVMReference = authVM
            val isDarkModeState by authVM.isDarkMode.collectAsStateWithLifecycle()
            val colorSchemeIndex by authVM.colorSchemeIndex.collectAsStateWithLifecycle()

            // Initial permission check
            LaunchedEffect(Unit) {
                val fineLocation = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                val coarseLocation = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION)
                authVM.setLocationPermissionGranted(
                    fineLocation == PackageManager.PERMISSION_GRANTED || coarseLocation == PackageManager.PERMISSION_GRANTED
                )
            }
            
            PathsTheme(
                darkTheme = isDarkModeState ?: isSystemInDarkTheme(),
                colorSchemeIndex = colorSchemeIndex
            ) {
                val locationPermissionGranted by authVM.locationPermissionGranted.collectAsStateWithLifecycle()
                
                LaunchedEffect(locationPermissionGranted) {
                    if (locationPermissionGranted) {
                        startLocationUpdates()
                    } else {
                        stopLocationUpdates()
                    }
                }
                
                AppNavigation(
                    stoperVM = viewModel(),
                    authVM = authVM,
                    onRequestLocationPermission = {
                        requestPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
            }
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onResume() {
        super.onResume()
        if (authVMReference?.locationPermissionGranted?.value == true) {
            startLocationUpdates()
        }
    }
}
