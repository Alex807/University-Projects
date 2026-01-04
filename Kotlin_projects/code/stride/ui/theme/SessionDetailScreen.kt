package com.example.stride.ui.theme

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stride.data.SensorSample
import com.example.stride.sensors.MovementMode
import com.example.stride.utils.DistanceCalculator.calculateDistance
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInteropFilter

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    session: SensorSample,
    onBack: () -> Unit
) {

    BackHandler(onBack = onBack) // to control the back OS button behavior

    val scrollState = rememberScrollState()

    // Track if user is interacting with the map
    var isMapTouched by remember { mutableStateOf(false) }

    // Simple safe GPS processing
    val hasGpsData = remember(session) {
        session.gpsCoordinates.isNotEmpty() &&
                session.gpsCoordinates.first().latitude != 0.0 &&
                session.gpsCoordinates.first().longitude != 0.0
    }

    val startPoint = remember(session) {
        try {
            if (hasGpsData) {
                LatLng(
                    session.gpsCoordinates.first().latitude,
                    session.gpsCoordinates.first().longitude
                )
            } else {
                LatLng(session.latitude, session.longitude)
            }
        } catch (e: Exception) {
            LatLng(0.0, 0.0)
        }
    }

    val endPoint = remember(session) {
        try {
            if (session.gpsCoordinates.size > 1) {
                LatLng(
                    session.gpsCoordinates.last().latitude,
                    session.gpsCoordinates.last().longitude
                )
            } else {
                startPoint
            }
        } catch (e: Exception) {
            startPoint
        }
    }

    // Filter route points - minimum 10m apart
    val routePoints = remember(session) {
        try {
            if (!hasGpsData || session.gpsCoordinates.size < 2) {
                emptyList()
            } else {
                val filtered = mutableListOf<LatLng>()
                filtered.add(LatLng(
                    session.gpsCoordinates.first().latitude,
                    session.gpsCoordinates.first().longitude
                ))

                for (i in 1 until session.gpsCoordinates.size) {
                    val current = LatLng(
                        session.gpsCoordinates[i].latitude,
                        session.gpsCoordinates[i].longitude
                    )
                    val previous = filtered.last()

                    val distance = calculateDistance(previous, current)

                    // Keep if >= 10m apart or last point
                    if (distance >= 10.0 || i == session.gpsCoordinates.size - 1) {
                        filtered.add(current)
                    }
                }

                filtered
            }
        } catch (e: Exception) {
            Log.e("SessionDetail", "Error filtering points: ${e.message}")
            emptyList()
        }
    }

    val centerPosition = remember(startPoint, endPoint) {
        LatLng(
            (startPoint.latitude + endPoint.latitude) / 2,
            (startPoint.longitude + endPoint.longitude) / 2
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(centerPosition, 15f)
    }

    val modeColor = remember(session.movementMode) {
        try {
            when (MovementMode.valueOf(session.movementMode)) {
                MovementMode.STATIONARY -> Color(0xFFB4E8B6)
                MovementMode.WALKING -> Color(0xFF4CAF50)
                MovementMode.JOGGING -> Color(0xFFFFC107)
                MovementMode.BICYCLE -> Color(0xFFFF5100)
                MovementMode.CAR_SLOW -> Color(0xFFF44336)
                MovementMode.CAR_FAST -> Color(0xFF366AC5)
                MovementMode.TRAIN -> Color(0xFF8631DA)
            }
        } catch (e: Exception) {
            Color(0xFF9E9E9E)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState, enabled = !isMapTouched)
        ) {
            // Map Section
            if (hasGpsData && startPoint.latitude != 0.0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val event = awaitPointerEvent()
                                        isMapTouched = event.changes.any { it.pressed }
                                    }
                                }
                            }
                    ) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(mapType = MapType.NORMAL),
                            uiSettings = MapUiSettings(
                                zoomControlsEnabled = true,
                                zoomGesturesEnabled = true,
                                scrollGesturesEnabled = true,
                                tiltGesturesEnabled = true,
                                rotationGesturesEnabled = true,
                                scrollGesturesEnabledDuringRotateOrZoom = true
                            )
                        ) {
                            // Draw route if we have points
                            if (routePoints.size >= 3) {
                                Polyline(
                                    points = routePoints,
                                    color = modeColor,
                                    width = 11f
                                )
                            }

                            // Start marker
                            Marker(
                                state = MarkerState(position = startPoint),
                                title = "Start",
                                snippet = session.startTime,
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                            )

                            // End marker (if different from start)
                            val distanceStartToEnd = calculateDistance(startPoint, endPoint)
                            if (distanceStartToEnd >= 30.0) {   // Minimum distance to show end marker
                                Marker(
                                    state = MarkerState(position = endPoint),
                                    title = "Finish",
                                    snippet = session.endTime,
                                    icon = BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_GREEN
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                // No GPS data placeholder
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📍 No GPS data",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Session Info Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard("Duration", formatDuration(session.durationSeconds),
                    "${session.startTime} - ${session.endTime}", Color(0xFF2196F3))

                //  Calculate total distance from ALL GPS coordinates (not filtered route points)
                val totalDistance = remember(session) {
                    try {
                        if (!hasGpsData || session.gpsCoordinates.size < 2) {
                            0.0
                        } else {
                            var distance = 0.0
                            var totalWeight = 0.0
                            for (i in 0 until session.gpsCoordinates.size - 1) {
                                val current = session.gpsCoordinates[i]
                                val next = session.gpsCoordinates[i + 1]

                                val currentLatLng = LatLng(current.latitude, current.longitude)
                                val nextLatLng = LatLng(next.latitude, next.longitude)

                                val segmentDistance = calculateDistance(currentLatLng, nextLatLng)

                                // Calculate weight based on accuracy (better accuracy = higher weight)
                                val avgAccuracy = (current.accuracy + next.accuracy) / 2f
                                val weight = if (avgAccuracy > 0) {
                                    1.0 / avgAccuracy  // Better accuracy = higher weight
                                } else {
                                    1.0
                                }

                                distance += segmentDistance * weight
                                totalWeight += weight
                            }

                            // Normalize by total weight
                            if (totalWeight > 0) distance / totalWeight else distance
                        }
                    } catch (e: Exception) {
                        Log.e("SessionDetail", "Error calculating distance: ${e.message}")
                        0.0
                    }
                }

                // Always show distance card if we have GPS data (even if 0)
                if (hasGpsData) {
                    InfoCard(
                        "Distance",
                        if (totalDistance < 1000) {
                            String.format("%.0f m", totalDistance)
                        } else {
                            String.format("%.2f km", totalDistance / 1000)
                        },
                        "${session.gpsCoordinates.size} GPS points",  //  Show point count
                        Color(0xFF00BCD4)
                    )
                }

                // Show accuracy statistics
                val avgAccuracy = remember(session) {
                    if (session.gpsCoordinates.isEmpty()) 0f
                    else session.gpsCoordinates.map { it.accuracy }.average().toFloat()
                }

                InfoCard(
                    "GPS Quality",
                    "±${String.format("%.1f", avgAccuracy)}m",
                    "Average accuracy",
                    Color(0xFF9C27B0)
                )

                InfoCard("Average Speed", String.format("%.1f km/h", session.averageSpeed * 3.6f),
                    String.format("%.2f m/s", session.averageSpeed), Color(0xFF4CAF50))

                InfoCard("Movement Mode", session.movementMode, "Detected automatically", modeColor)

                InfoCard("Location", session.city.ifBlank { "Unknown" },
                    "${session.streetName} ${session.streetNumber}".trim(), Color(0xFFFF9800))

                InfoCard("Date", session.date.split(" ").firstOrNull() ?: session.date,
                    "Session recorded", Color(0xFF607D8B))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoCard(title: String, value: String, subtitle: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
    }
}

@SuppressLint("DefaultLocale")
private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return when {
        hours > 0 -> String.format("%dh %dm %ds", hours, minutes, secs)
        minutes > 0 -> String.format("%dm %ds", minutes, secs)
        else -> String.format("%ds", secs)
    }
}