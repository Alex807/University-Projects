package com.example.stride

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.stride.auth.UserPreferences
import com.example.stride.data.AppDatabase
import com.example.stride.service.TrackingService
import com.example.stride.ui.theme.LoginScreen
import com.example.stride.ui.theme.RegisterScreen
import com.example.stride.ui.theme.SessionDetailScreen
import com.example.stride.ui.theme.StrideTheme
import com.example.stride.viewmodel.AuthViewModel
import com.example.stride.viewmodel.SensorViewModel
import com.example.stride.viewmodel.SensorViewModelFactory

class MainActivity : ComponentActivity() {

    private val notificationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.w("MainActivity", "Notification permission denied")
        }
    }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                startSensorsIfNeeded()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                startSensorsIfNeeded()
            }
            else -> {
                Log.w("MainActivity", "Location permission denied")
            }
        }
    }

    private var sensorViewModel: SensorViewModel? = null
    private lateinit var userPreferences: UserPreferences
    private var navController: NavHostController? = null
    private var pendingSessionId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userPreferences = UserPreferences(applicationContext)

        Log.d("MainActivity", "onCreate - isLoggedIn: ${userPreferences.isLoggedIn()}")

        // Check if opened from notification
        handleIntent(intent)

        setContent {
            StrideTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: AuthViewModel = viewModel(
                        factory = SensorViewModelFactory(application)
                    )

                    val navControllerLocal = rememberNavController()
                    navController = navControllerLocal

                    // Navigate to session details if pending
                    LaunchedEffect(pendingSessionId) {
                        pendingSessionId?.let { sessionId ->
                            if (authViewModel.checkLoginStatus()) {
                                Log.d("MainActivity", "Navigating to session details: $sessionId")
                                navControllerLocal.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                                // Small delay to ensure main screen is loaded
                                kotlinx.coroutines.delay(500)
                                navControllerLocal.navigate("sessionDetails/$sessionId")
                                pendingSessionId = null
                            }
                        }
                    }

                    NavHost(
                        navController = navControllerLocal,
                        startDestination = if (authViewModel.checkLoginStatus()) "main" else "login"
                    ) {
                        composable("login") {
                            BackHandler {
                                moveTaskToBack(true)
                            }

                            LoginScreen(
                                authViewModel = authViewModel,
                                onLoginSuccess = {
                                    navControllerLocal.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                    checkAndRequestPermissions()
                                },
                                onNavigateToRegister = {
                                    navControllerLocal.navigate("register")
                                }
                            )
                        }

                        composable("register") {
                            BackHandler {
                                navControllerLocal.navigate("login") {
                                    popUpTo("register") { inclusive = true }
                                }
                            }

                            RegisterScreen(
                                authViewModel = authViewModel,
                                onRegisterSuccess = {
                                    navControllerLocal.navigate("main") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                    checkAndRequestPermissions()
                                },
                                onNavigateToLogin = {
                                    navControllerLocal.navigate("login") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("main") {
                            BackHandler {
                                moveTaskToBack(true)
                            }

                            val vm: SensorViewModel = viewModel(
                                factory = SensorViewModelFactory(application)
                            )

                            val isTracking by vm.isTracking.collectAsState()

                            LaunchedEffect(vm) {
                                sensorViewModel = vm

                                // Set initial foreground state
                                vm.setAppInForeground(true)

                                if (authViewModel.checkLoginStatus() && hasLocationPermission()) {
                                    Log.d("MainActivity", "Starting sensors on main screen load")
                                    vm.startSensors()
                                }
                            }

                            DisposableEffect(isTracking) {
                                Log.d("MainActivity", "Tracking state changed: $isTracking")
                                onDispose { }
                            }

                            Stage2Screen(
                                viewModel = vm,
                                onLogout = {
                                    Log.d("MainActivity", "Logging out - stopping sensors")
                                    vm.stopSensors()
                                    userPreferences.setSensorsStarted(false)
                                    authViewModel.logout()
                                    navControllerLocal.navigate("login") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(
                            route = "sessionDetails/{sessionId}",
                            arguments = listOf(
                                navArgument("sessionId") {
                                    type = NavType.IntType
                                }
                            )
                        ) { backStackEntry ->
                            val sessionId = backStackEntry.arguments?.getInt("sessionId") ?: -1
                            val database = AppDatabase.getDatabase(applicationContext)
                            val sampleDao = database.sensorSampleDao()

                            var session by remember { mutableStateOf<com.example.stride.data.SensorSample?>(null) }

                            LaunchedEffect(sessionId) {
                                session = sampleDao.getSessionById(sessionId)
                                Log.d("MainActivity", "Loaded session: $sessionId")
                            }

                            session?.let {
                                SessionDetailScreen(
                                    session = it,
                                    onBack = {
                                        navControllerLocal.popBackStack()
                                    }
                                )
                            } ?: run {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            TrackingService.ACTION_OPEN_SESSION_DETAILS -> {
                val sessionId = intent.getIntExtra(TrackingService.EXTRA_SESSION_ID, -1)
                if (sessionId != -1) {
                    Log.d("MainActivity", "Received session ID from notification: $sessionId")

                    // Dismiss the notification if requested
                    if (intent.getBooleanExtra("AUTO_DISMISS_NOTIFICATION", false)) {
                        val notificationManager =
                            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.cancel(2002) // SAVED_NOTIFICATION_ID
                    }
                    pendingSessionId = sessionId

                    // If already logged in and navController is ready, navigate immediately
                    if (userPreferences.isLoggedIn() && navController != null) {
                        navController?.navigate("sessionDetails/$sessionId") {
                            launchSingleTop = true
                        }
                        pendingSessionId = null
                    }
                }
            }

            TrackingService.ACTION_OPEN_HOME -> {
                Log.d("MainActivity", "Opening home screen from notification")
                // Just open the app to main screen - no navigation needed
                if (userPreferences.isLoggedIn() && navController != null) {
                    navController?.navigate("main") {
                        popUpTo("main") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        Log.d("MainActivity", "onResume - App in FOREGROUND")

        // Notify ViewModel that app is in foreground (enable vibrations)
        sensorViewModel?.setAppInForeground(true)

        if (userPreferences.isLoggedIn() && hasLocationPermission()) {
            sensorViewModel?.let { vm ->
                Log.d("MainActivity", "onResume - restarting sensors")
                vm.startSensors()
            }
        }
    }

    override fun onPause() {
        super.onPause()

        Log.d("MainActivity", "onPause - App in BACKGROUND")

        // Notify ViewModel that app is in background (disable vibrations)
        sensorViewModel?.setAppInForeground(false)

        val isTracking = sensorViewModel?.isTracking?.value ?: false

        if (isTracking) {
            Log.d("MainActivity", "onPause - keeping sensors running (recording active)")
        } else {
            Log.d("MainActivity", "onPause - stopping sensors (no active recording)")
            sensorViewModel?.stopSensors()
        }
    }

    override fun onStop() {
        super.onStop()

        Log.d("MainActivity", "onStop")

        val isTracking = sensorViewModel?.isTracking?.value ?: false

        if (!isTracking) {
            Log.d("MainActivity", "onStop - stopping sensors (no active recording)")
            sensorViewModel?.stopSensors()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("MainActivity", "onDestroy - isFinishing: $isFinishing")

        if (isFinishing) {
            val isTracking = sensorViewModel?.isTracking?.value ?: false
            if (!isTracking) {
                Log.d("MainActivity", "onDestroy - stopping sensors (activity finishing)")
                sensorViewModel?.stopSensors()
            }
        }

        sensorViewModel = null
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // ADD NOTIFICATION PERMISSION FOR ANDROID 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            locationPermissionRequest.launch(permissionsToRequest.toTypedArray())
        } else {
            startSensorsIfNeeded()
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

    private fun startSensorsIfNeeded() {
        if (hasLocationPermission() && userPreferences.isLoggedIn()) {
            Log.d("MainActivity", "Starting sensors")
            sensorViewModel?.startSensors()
            userPreferences.setSensorsStarted(true)
        }
    }
}