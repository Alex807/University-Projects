package com.example.stride.ui.theme

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.stride.data.SensorSample
import com.example.stride.sensors.MovementMode
import com.example.stride.viewmodel.SensorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsListScreen(
    viewModel: SensorViewModel,
    onBack: () -> Unit
) {

    BackHandler(onBack = onBack) // to control the back OS button behavior

    val sessions by viewModel.sessions.collectAsState()
    var selectedSession by remember { mutableStateOf<SensorSample?>(null) }
    var sessionToDelete by remember { mutableStateOf<SensorSample?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadSessions()
    }

    // Show detail screen if session is selected
    selectedSession?.let { session ->
        SessionDetailScreen(
            session = session,
            onBack = { selectedSession = null }
        )
        return
    }

    // Delete confirmation dialog
    sessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Delete Session") },
            text = { Text("Are you sure you want to delete this session?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSession(session)
                        sessionToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Sessions") },
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
        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No sessions yet",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "Start tracking to create your first session",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(sessions) { session ->
                    SessionCard(
                        session = session,
                        onClick = { selectedSession = session },
                        onDelete = { sessionToDelete = session }
                    )
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun SessionCard(
    session: SensorSample,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val modeColor = when (MovementMode.valueOf(session.movementMode)) {
        MovementMode.STATIONARY -> Color(0xFF9E9E9E)
        MovementMode.WALKING -> Color(0xFF4CAF50)
        MovementMode.JOGGING -> Color(0xFFFFC107)
        MovementMode.BICYCLE -> Color(0xFFFF9800)
        MovementMode.CAR_SLOW -> Color(0xFFF44336)
        MovementMode.CAR_FAST -> Color(0xFF366AC5)
        MovementMode.TRAIN -> Color(0xFF8631DA)
    }

    val distanceMeters = session.totalDistanceMeters

    // Format distance
    val distanceText = if (distanceMeters >= 1000) {
        String.format("%.2f km", distanceMeters / 1000)
    } else {
        String.format("%.0f m", distanceMeters)
    }

    // Format location as "Start City - End City"
    val startLocation = session.city

    val endLocation = if (session.endCity.isNotEmpty() && session.endCity != "Unknown") {
        if (session.endStreetName.isNotEmpty() && session.endStreetName != "Unknown" && session.endStreetName != "Offline") {
            "${session.endCity}, ${session.endStreetName}"
        } else {
            session.endCity
        }
    } else {
        session.city  // Fallback to start city if end city not available
    }

    val locationDisplay = if (startLocation == endLocation) {
        startLocation  // Same location, show once
    } else {
        "$startLocation → $endLocation"  // Different locations, show route
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = modeColor.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Movement Mode Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = modeColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = session.movementMode,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = modeColor
                    )
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date and Time
            Text(
                text = session.date,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Duration and Time Range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Duration",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = formatDuration(session.durationSeconds),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Time Range",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "${session.startTime} - ${session.endTime}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Divider
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(12.dp))

            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(
                    label = "Avg Speed",
                    value = String.format("%.1f km/h", session.averageSpeed * 3.6f),
                    color = Color(0xFF4CAF50)
                )

                StatItem(
                    label = "Total Distance",
                    value = distanceText,
                    color = Color(0xFF2196F3)
                )

                // Location display - CHANGED to show route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Route",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = locationDisplay,
                        fontSize = 12.sp,  // Slightly smaller for longer text
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800),
                        maxLines = 2,  // Allow 2 lines for long city names
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom accent bar for visual separation
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = modeColor.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                ) {}
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
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