package com.example.smartmotiondetector.ui.theme

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartmotiondetector.data.SensorSample
import com.example.smartmotiondetector.sensors.MovementMode
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlin.math.*
import com.example.smartmotiondetector.R
import com.google.android.gms.maps.model.BitmapDescriptorFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    session: SensorSample,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

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

    // Filter route points - minimum 50m apart
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

                    // Keep if >= 50m apart or last point
                    if (distance >= 50.0 || i == session.gpsCoordinates.size - 1) {
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(scrollState)
        ) {
            // Map Section
            if (hasGpsData && startPoint.latitude != 0.0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        properties = MapProperties(mapType = MapType.NORMAL),
                        uiSettings = MapUiSettings(zoomControlsEnabled = true)
                    ) {
                        // Draw route if we have points
                        if (routePoints.size >= 2) {
                            Polyline(
                                points = routePoints,
                                color = modeColor,
                                width = 10f
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
                        if (startPoint != endPoint && calculateDistance(startPoint, endPoint) > 50.0) {
                            Marker(
                                state = MarkerState(position = endPoint),
                                title = "Finish",
                                snippet = session.endTime,
                                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)                            )
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

                // Calculate total route distance
                val totalDistance = remember(routePoints) {
                    if (routePoints.size < 2) {
                        0.0
                    } else {
                        var distance = 0.0
                        for (i in 0 until routePoints.size - 1) {
                            distance += calculateDistance(routePoints[i], routePoints[i + 1])
                        }
                        distance
                    }
                }

                // Add this InfoCard
                if (totalDistance > 0) {
                    InfoCard(
                        "Distance",
                        if (totalDistance < 1000) {
                            String.format("%.0f m", totalDistance)
                        } else {
                            String.format("%.2f km", totalDistance / 1000)
                        },
                        "",
                        Color(0xFF00BCD4)
                    )
                }

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

// Simple distance calculation in meters
private fun calculateDistance(start: LatLng, end: LatLng): Double {
    val earthRadius = 6371000.0 // meters
    val dLat = Math.toRadians(end.latitude - start.latitude)
    val dLon = Math.toRadians(end.longitude - start.longitude)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(start.latitude)) * cos(Math.toRadians(end.latitude)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadius * c
}