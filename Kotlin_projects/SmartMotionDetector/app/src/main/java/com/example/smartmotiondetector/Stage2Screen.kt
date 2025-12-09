package com.example.smartmotiondetector

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartmotiondetector.sensors.GpsSpeedFilter
import com.example.smartmotiondetector.sensors.MovementMode
import com.example.smartmotiondetector.ui.theme.SessionsListScreen
import com.example.smartmotiondetector.ui.theme.StatsScreen
import com.example.smartmotiondetector.utils.ImageCompressor
import com.example.smartmotiondetector.viewmodel.AuthViewModel
import com.example.smartmotiondetector.viewmodel.SensorViewModel
import com.example.smartmotiondetector.viewmodel.SensorViewModelFactory

@Composable
fun Stage2Screen(
    viewModel: SensorViewModel,
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val saveStatus by viewModel.saveStatus.collectAsState()
    val countStatus by viewModel.countStatus.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()
    var showSessionsList by remember { mutableStateOf(false) }
    var showStatsScreen by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val authViewModel: AuthViewModel = viewModel(
        factory = SensorViewModelFactory(context.applicationContext as Application)
    )
    val profileImageBase64 = remember { authViewModel.getUserProfileImage() }

    if (showSessionsList) {
        SessionsListScreen(
            viewModel = viewModel,
            onBack = { showSessionsList = false }
        )
        return
    }

    if (showStatsScreen) {
        StatsScreen(
            viewModel = viewModel,
            onBack = { showStatsScreen = false }
        )
        return
    }

    val speedFilter = remember { GpsSpeedFilter() }
    val displaySpeedKmh = speedFilter.getSpeedKmh(uiState.fusedSpeed)

    val speedColor = when {
        displaySpeedKmh < 1.0 -> Color(0xFF9E9E9E)
        displaySpeedKmh < 5.0 -> Color(0xFF4CAF50)
        displaySpeedKmh < 20.0 -> Color(0xFFFFC107)
        displaySpeedKmh < 50.0 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    val modeColor = when (uiState.movementMode) {
        MovementMode.STATIONARY -> Color(0xFF9E9E9E)
        MovementMode.WALKING -> Color(0xFF4CAF50)
        MovementMode.JOGGING -> Color(0xFFFFC107)
        MovementMode.BICYCLE -> Color(0xFFFF9800)
        MovementMode.CAR_SLOW -> Color(0xFFF44336)
        MovementMode.CAR_FAST -> Color(0xFF366AC5)
        MovementMode.TRAIN -> Color(0xFF8631DA)
    }

    // GPS Quality based on satellite count
    val (qualityText, qualityColor) = when {
        uiState.satelliteCount >= 12 -> "Excellent" to Color(0xFF4CAF50)
        uiState.satelliteCount >= 8 -> "Very Good" to Color(0xFF8BC34A)
        uiState.satelliteCount >= 6 -> "Good" to Color(0xFFFFC107)
        uiState.satelliteCount >= 4 -> "Fair" to Color(0xFFFF9800)
        uiState.satelliteCount >= 2 -> "Poor" to Color(0xFFFF5722)
        else -> "No Signal" to Color(0xFFF44336)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with profile and logout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Motion Detector",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profileImageBase64?.let { base64 ->
                        val bitmap = ImageCompressor.decodeBase64ToBitmap(base64)
                        bitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    TextButton(
                        onClick = onLogout,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Logout",
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Speed Display
            Card(
                modifier = Modifier.size(220.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (displaySpeedKmh < 10) {
                                String.format("%.1f", displaySpeedKmh)
                            } else {
                                displaySpeedKmh.toInt().toString()
                            },
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Bold,
                            color = speedColor
                        )
                        Text(
                            text = "km/h",
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Movement Mode
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = modeColor.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = getMovementModeIcon(uiState.movementMode)),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = modeColor
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = uiState.movementMode.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = modeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // GPS Quality - Updated with adjectives
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "GPS Signal",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = qualityText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = qualityColor
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Satellites",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${uiState.satelliteCount}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = qualityColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Messages
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    saveStatus != null -> {
                        Text(
                            text = saveStatus!!,
                            color = if (saveStatus!!.contains("Error"))
                                MaterialTheme.colorScheme.error
                            else
                                Color(0xFF4CAF50),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    countStatus != null -> {
                        Text(
                            text = countStatus!!,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Action Buttons - UPDATED
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start/Pause Button - RED for Start, YELLOW for Pause
                Button(
                    onClick = {
                        if (isTracking) {
                            viewModel.stopTrackingAndSave()
                        } else {
                            viewModel.startTracking()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTracking) Color(0xFFFFC107) else Color(0xFFF44336)
                    ),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isTracking)
                                android.R.drawable.ic_media_pause
                            else
                                android.R.drawable.ic_media_play
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTracking) "Pause" else "Start",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Stats Button (replaced Count)
                Button(
                    onClick = { showStatsScreen = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text(
                        text = "Stats",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // List Button
                Button(
                    onClick = { showSessionsList = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9800)
                    ),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Text(
                        text = "List",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun getMovementModeIcon(mode: MovementMode): Int {
    return when (mode) {
        MovementMode.STATIONARY -> com.example.smartmotiondetector.R.drawable.ic_stationary
        MovementMode.WALKING -> com.example.smartmotiondetector.R.drawable.ic_walking
        MovementMode.JOGGING -> com.example.smartmotiondetector.R.drawable.ic_jogging
        MovementMode.BICYCLE -> com.example.smartmotiondetector.R.drawable.ic_bicycle
        MovementMode.CAR_SLOW -> com.example.smartmotiondetector.R.drawable.ic_car
        MovementMode.CAR_FAST -> com.example.smartmotiondetector.R.drawable.ic_car_fast
        MovementMode.TRAIN -> com.example.smartmotiondetector.R.drawable.ic_train
    }
}