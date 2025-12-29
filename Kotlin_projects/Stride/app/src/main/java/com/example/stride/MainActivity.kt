package com.example.stride

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stride.ui.theme.LoginScreen
import com.example.stride.ui.theme.RegisterScreen
import com.example.stride.ui.theme.SmartMotionDetectorTheme
import com.example.stride.viewmodel.AuthViewModel
import com.example.stride.viewmodel.SensorViewModel
import com.example.stride.viewmodel.SensorViewModelFactory

class MainActivity : ComponentActivity() {

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                startSensorsIfPermitted()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                startSensorsIfPermitted()
            }
            else -> {
                // No location access granted
            }
        }
    }

    private var sensorViewModel: SensorViewModel? = null
    private var sensorsStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorsStarted = savedInstanceState?.getBoolean("sensorsStarted") ?: false

        setContent {
            SmartMotionDetectorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: AuthViewModel = viewModel(
                        factory = SensorViewModelFactory(application)
                    )

                    var currentScreen by remember {
                        mutableStateOf(
                            if (authViewModel.checkLoginStatus()) "main" else "login"
                        )
                    }

                    when (currentScreen) {
                        "login" -> {
                            LoginScreen(
                                authViewModel = authViewModel,
                                onLoginSuccess = {
                                    currentScreen = "main"
                                    checkAndRequestPermissions()
                                },
                                onNavigateToRegister = { currentScreen = "register" }
                            )
                        }
                        "register" -> {
                            RegisterScreen(
                                authViewModel = authViewModel,
                                onRegisterSuccess = {
                                    currentScreen = "main"
                                    checkAndRequestPermissions()
                                },
                                onNavigateToLogin = { currentScreen = "login" }
                            )
                        }
                        "main" -> {
                            val vm: SensorViewModel = viewModel(
                                factory = SensorViewModelFactory(application)
                            )

                            LaunchedEffect(vm) {
                                sensorViewModel = vm
                                if (sensorsStarted) {
                                    Log.d("MainActivity", "Restarting sensors after config change")
                                    vm.startSensors()
                                }
                            }

                            Stage2Screen(
                                viewModel = vm,
                                onLogout = {
                                    authViewModel.logout()
                                    currentScreen = "login"
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("sensorsStarted", sensorsStarted)
    }

    override fun onResume() {
        super.onResume()
        if (sensorsStarted && hasLocationPermission()) {
            Log.d("MainActivity", "onResume - restarting sensors")
            sensorViewModel?.startSensors()
        }
    }

    private fun checkAndRequestPermissions() {
        if (!hasLocationPermission()) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            startSensorsIfPermitted()
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startSensorsIfPermitted() {
        if (hasLocationPermission() && !sensorsStarted) {
            Log.d("MainActivity", "Starting sensors for first time")
            sensorViewModel?.startSensors()
            sensorsStarted = true
        }
    }
}