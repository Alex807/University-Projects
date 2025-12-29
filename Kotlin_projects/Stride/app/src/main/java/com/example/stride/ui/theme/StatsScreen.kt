package com.example.stride.ui.theme

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.stride.sensors.MovementMode
import com.example.stride.stats.SessionStats
import com.example.stride.stats.StatsCalculator
import com.example.stride.stats.TimeFilter
import com.example.stride.viewmodel.SensorViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: SensorViewModel,
    onBack: () -> Unit
) {
    val sessions by viewModel.sessions.collectAsState()
    var selectedFilter by remember { mutableStateOf(TimeFilter.ALL_TIME) }
    val statsCalculator = remember { StatsCalculator() }
    val stats = remember(sessions, selectedFilter) {
        statsCalculator.calculateStats(sessions, selectedFilter)
    }

    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.loadSessions()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Time Filter Chips
            TimeFilterRow(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it }
            )

            if (stats.totalSessions == 0) {
                NoDataCard()
            } else {
                // Overview Cards
                OverviewCards(stats)

                // Movement Mode Distribution Chart
                MovementModeChart(stats)

                // Speed Distribution Chart
                SpeedDistributionChart(stats)

                // Favorite Locations
                FavoriteLocationsCard(stats)

                // Highlights
                HighlightsCard(stats)
            }
        }
    }
}

@Composable
private fun TimeFilterRow(
    selectedFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TimeFilter.values().forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = when (filter) {
                            TimeFilter.THIS_HOUR -> "This Hour"
                            TimeFilter.TODAY -> "Today"
                            TimeFilter.THIS_WEEK -> "This Week"
                            TimeFilter.THIS_MONTH -> "This Month"
                            TimeFilter.ALL_TIME -> "All Time"
                        }
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@Composable
private fun NoDataCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "No data available",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "Try selecting a different time filter",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
private fun OverviewCards(stats: SessionStats) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Sessions",
                value = "${stats.totalSessions}",
                color = Color(0xFF2196F3),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Duration",
                value = formatDuration(stats.totalDuration),
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Distance",
                value = String.format("%.1f km", stats.totalDistance),
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Avg Speed",
                value = String.format("%.1f km/h", stats.averageSpeed),
                color = Color(0xFF9C27B0),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Max Speed",
                value = String.format("%.1f km/h", stats.maxSpeed),
                color = Color(0xFFF44336),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "GPS Points",
                value = "${stats.totalGpsPoints}",
                color = Color(0xFF00BCD4),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun MovementModeChart(stats: SessionStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Movement Modes",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            AndroidView(
                factory = { context ->
                    PieChart(context).apply {
                        description.isEnabled = false
                        setDrawEntryLabels(true)
                        setEntryLabelColor(AndroidColor.BLACK)
                        setEntryLabelTextSize(12f)
                        legend.isEnabled = true
                        legend.textSize = 12f
                        setUsePercentValues(true)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                update = { chart ->
                    val entries = stats.movementModeDistribution.map { (mode, count) ->
                        PieEntry(count.toFloat(), mode.name)
                    }

                    val colors = stats.movementModeDistribution.keys.map { mode ->
                        when (mode) {
                            MovementMode.STATIONARY -> AndroidColor.parseColor("#9E9E9E")
                            MovementMode.WALKING -> AndroidColor.parseColor("#4CAF50")
                            MovementMode.JOGGING -> AndroidColor.parseColor("#FFC107")
                            MovementMode.BICYCLE -> AndroidColor.parseColor("#FF9800")
                            MovementMode.CAR_SLOW -> AndroidColor.parseColor("#F44336")
                            MovementMode.CAR_FAST -> AndroidColor.parseColor("#366AC5")
                            MovementMode.TRAIN -> AndroidColor.parseColor("#8631DA")
                        }
                    }

                    val dataSet = PieDataSet(entries, "").apply {
                        setColors(colors)
                        valueTextSize = 14f
                        valueTextColor = AndroidColor.WHITE
                    }

                    chart.data = PieData(dataSet).apply {
                        setValueFormatter(PercentFormatter(chart))
                    }
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
private fun SpeedDistributionChart(stats: SessionStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Speed Distribution",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            AndroidView(
                factory = { context ->
                    BarChart(context).apply {
                        description.isEnabled = false
                        setDrawGridBackground(false)
                        legend.isEnabled = false
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.setDrawGridLines(false)
                        xAxis.granularity = 1f
                        xAxis.textSize = 10f
                        axisLeft.setDrawGridLines(true)
                        axisLeft.granularity = 1f
                        axisRight.isEnabled = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                update = { chart ->
                    val entries = stats.speedDistribution.entries.mapIndexed { index, entry ->
                        BarEntry(index.toFloat(), entry.value.toFloat())
                    }

                    val labels = stats.speedDistribution.keys.toList()

                    val dataSet = BarDataSet(entries, "Speed Ranges").apply {
                        color = AndroidColor.parseColor("#2196F3")
                        valueTextSize = 12f
                        valueTextColor = AndroidColor.BLACK
                    }

                    chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                    chart.data = BarData(dataSet)
                    chart.invalidate()
                }
            )
        }
    }
}

@Composable
private fun FavoriteLocationsCard(stats: SessionStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Favorite Locations",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (stats.favoriteLocations.isEmpty()) {
                Text(
                    text = "No location data available",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                stats.favoriteLocations.forEach { location ->
                    LocationItem(location)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun LocationItem(location: com.example.stride.stats.LocationFrequency) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = location.city,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${location.count} sessions",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Text(
            text = String.format("%.1f%%", location.percentage),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )
    }
}

@Composable
private fun HighlightsCard(stats: SessionStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Highlights",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            stats.longestSession?.let { session ->
                HighlightItem(
                    title = "Longest Session",
                    value = formatDuration(session.durationSeconds),
                    subtitle = "${session.city} • ${session.date.split(" ").firstOrNull()}",
                    color = Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            stats.fastestSession?.let { session ->
                HighlightItem(
                    title = "Fastest Session",
                    value = String.format("%.1f km/h", session.averageSpeed * 3.6f),
                    subtitle = "${session.movementMode} • ${session.city}",
                    color = Color(0xFFF44336)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            stats.mostActiveDay?.let { day ->
                val count = stats.sessionsPerDay[day] ?: 0
                HighlightItem(
                    title = "Most Active Day",
                    value = day,
                    subtitle = "$count sessions recorded",
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

@Composable
private fun HighlightItem(
    title: String,
    value: String,
    subtitle: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return when {
        hours > 0 -> String.format("%dh %dm", hours, minutes)
        minutes > 0 -> String.format("%dm %ds", minutes, secs)
        else -> String.format("%ds", secs)
    }
}