package com.example.stride

import android.app.Application
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stride.sensors.GpsSpeedFilter
import com.example.stride.sensors.MovementMode
import com.example.stride.ui.theme.SessionsListScreen
import com.example.stride.ui.theme.StatsScreen
import com.example.stride.utils.ImageCompressor
import com.example.stride.viewmodel.AuthViewModel
import com.example.stride.viewmodel.SensorViewModel
import com.example.stride.viewmodel.SensorViewModelFactory

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
    var showManageAccountsDialog by remember { mutableStateOf(false) }

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
                                    .size(65.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Logout Button
                        Card(
                            modifier = Modifier,
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            TextButton(
                                onClick = onLogout,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Text(
                                    text = "Logout",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Manage Accounts Button
                        Card(
                            modifier = Modifier,
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            TextButton(
                                onClick = { showManageAccountsDialog = true },
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Text(
                                    text = "Manage Accounts",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
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
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isTracking)
                                android.R.drawable.ic_media_pause
                            else
                                android.R.drawable.ic_media_play
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(21.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isTracking) "Pause" else "Start",
                        fontSize = 18.sp,
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
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text(
                        text = "Stats",
                        fontSize = 18.sp,
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
                    contentPadding = PaddingValues(vertical = 14.dp)
                ) {
                    Text(
                        text = "List",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
    // Manage Accounts Dialog
    if (showManageAccountsDialog) {
        ManageAccountsDialog(
            authViewModel = authViewModel,
            onDismiss = { showManageAccountsDialog = false }
        )
    }
}

@Composable
fun ManageAccountsDialog(
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    val savedAccounts by authViewModel.savedAccounts.collectAsState()
    var selectedAccounts by remember { mutableStateOf(setOf<String>()) }
    var showConfirmation by remember { mutableStateOf(false) }

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Confirm Deletion") },
            text = {
                Text(
                    "Are you sure you want to delete ${selectedAccounts.size} " +
                            "account${if (selectedAccounts.size > 1) "s" else ""} from Remember Me?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel.removeSavedAccounts(selectedAccounts.toList())
                        showConfirmation = false
                        onDismiss()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    } else {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "Manage Saved Accounts",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (savedAccounts.isEmpty()) {
                        Text(
                            text = "No saved accounts found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        Text(
                            text = "Select accounts to delete:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                        ) {
                            items(savedAccounts) { account ->
                                // Process image outside composable context
                                val profileBitmap = remember(account.profileImageBase64) {
                                    account.profileImageBase64?.let {
                                        try {
                                            val imageBytes = android.util.Base64.decode(it, android.util.Base64.DEFAULT)
                                            android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedAccounts = if (account.username in selectedAccounts) {
                                                selectedAccounts - account.username
                                            } else {
                                                selectedAccounts + account.username
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = account.username in selectedAccounts,
                                        onCheckedChange = {
                                            selectedAccounts = if (it) {
                                                selectedAccounts + account.username
                                            } else {
                                                selectedAccounts - account.username
                                            }
                                        }
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Profile Image
                                    if (profileBitmap != null) {
                                        Image(
                                            bitmap = profileBitmap.asImageBitmap(),
                                            contentDescription = "Profile picture",
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        PlaceholderProfileImageForManage()
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = account.username,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Close")
                        }

                        if (savedAccounts.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (selectedAccounts.isNotEmpty()) {
                                        showConfirmation = true
                                    }
                                },
                                enabled = selectedAccounts.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceholderProfileImageForManage() {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Default profile",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun getMovementModeIcon(mode: MovementMode): Int {
    return when (mode) {
        MovementMode.STATIONARY -> com.example.stride.R.drawable.ic_stationary
        MovementMode.WALKING -> com.example.stride.R.drawable.ic_walking
        MovementMode.JOGGING -> com.example.stride.R.drawable.ic_jogging
        MovementMode.BICYCLE -> com.example.stride.R.drawable.ic_bicycle
        MovementMode.CAR_SLOW -> com.example.stride.R.drawable.ic_car
        MovementMode.CAR_FAST -> com.example.stride.R.drawable.ic_car_fast
        MovementMode.TRAIN -> com.example.stride.R.drawable.ic_train
    }
}