package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.unit.sp
import com.example.model.Stats
import com.example.model.Task
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val stats by viewModel.stats.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val appUsage by viewModel.appUsage.collectAsState()
    val liveSteps by viewModel.liveSteps.collectAsState()
    val hasUsagePermission by viewModel.hasUsagePermission.collectAsState()
    val hasActivityPermission by viewModel.hasActivityPermission.collectAsState()
    
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasRequestedActivityPermission by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

    val activityPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updateActivityPermissionState(isGranted)
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val usageGranted = AppUsageUtils.hasUsageStatsPermission(context)
            viewModel.updateUsagePermissionState(usageGranted)
            if (usageGranted) {
                viewModel.fetchAppUsage()
            }

            val activityGranted = context.checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION) == android.content.pm.PackageManager.PERMISSION_GRANTED
            viewModel.updateActivityPermissionState(activityGranted)
            if (!activityGranted && !hasRequestedActivityPermission) {
                hasRequestedActivityPermission = true
                activityPermissionLauncher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
    }
    
    val pendingTasks = tasks.filter { !it.completed }
    val currentStats = stats ?: Stats()
    
    val now = Date()
    val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm", Locale.getDefault())
    val amPmFormat = SimpleDateFormat("a", Locale.getDefault())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Header(
                dateStr = dateFormat.format(now).uppercase(),
                timeStr = timeFormat.format(now),
                amPmStr = amPmFormat.format(now),
                streak = currentStats.currentStreak
            )
        }
        
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.CheckCircle,
                    iconTint = Emerald400,
                    label = "Tasks Today",
                    value = "${currentStats.tasksCompleted} done",
                    subtext = "${pendingTasks.size} pending",
                    containerColor = Emerald500.copy(alpha = 0.1f)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.LocalFireDepartment,
                    iconTint = Indigo400,
                    label = "Focus",
                    value = "${String.format("%.1f", currentStats.focusHours)}h",
                    subtext = "deep work",
                    containerColor = Indigo500.copy(alpha = 0.1f)
                )
            }
        }
        
        item {
            ActivityHeatmap(streak = currentStats.currentStreak)
        }

        item {
            AppUsageSection(appUsage = appUsage, hasPermission = hasUsagePermission)
        }
        
        item {
            WellbeingSection(
                stats = currentStats,
                liveSteps = liveSteps,
                hasPermission = hasActivityPermission,
                onAddWater = { viewModel.addWater(8) }
            )
        }
        
        item {
            UpNextSection(pendingTasks = pendingTasks)
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
private fun Header(dateStr: String, timeStr: String, amPmStr: String, streak: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = dateStr,
            color = Slate400,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Light,
                    color = Color.White
                )
                Text(
                    text = amPmStr,
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate500,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
            }
            
            Row(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Orange500.copy(alpha = 0.1f))
                    .border(1.dp, Orange500.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = Orange400,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "$streak Day${if (streak != 1) "s" else ""}",
                    color = Orange400,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    subtext: String,
    containerColor: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(containerColor)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = Slate300
        )
        Text(
            text = subtext,
            style = MaterialTheme.typography.labelSmall,
            color = Slate400,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ActivityHeatmap(streak: Int) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = Orange400,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Activity Heatmap",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
        
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Slate800.copy(alpha = 0.5f))
                .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Current Streak", color = Slate400, style = MaterialTheme.typography.labelMedium)
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = streak.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = " days",
                            color = Slate500,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Target", color = Slate400, style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "90 days",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = Slate300
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Heatmap grid (simplified for Android)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (col in 0 until 12) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (row in 0 until 7) {
                            val dayNumber = col * 7 + row + 1
                            val isCompleted = dayNumber <= streak
                            val isToday = dayNumber == streak + 1
                            
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when {
                                            isCompleted -> Orange500
                                            isToday -> Orange500.copy(alpha = 0.2f)
                                            else -> Slate800
                                        }
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = when {
                                            isToday -> Orange500.copy(alpha = 0.5f)
                                            !isCompleted -> Slate700.copy(alpha = 0.5f)
                                            else -> Color.Transparent
                                        },
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppUsageSection(appUsage: List<Pair<String, Long>>, hasPermission: Boolean) {
    val context = LocalContext.current
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
            Icon(
                imageVector = Icons.Filled.PhoneAndroid,
                contentDescription = null,
                tint = Indigo400,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "App Usage (Screen Time)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Slate800.copy(alpha = 0.5f))
                .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(20.dp)
                .clickable {
                    if (!hasPermission) {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    }
                },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!hasPermission) {
                Text(
                    text = "Tap to enable Usage Access to see your real screen time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate400
                )
            } else if (appUsage.isEmpty()) {
                Text(
                    text = "No app usage data available yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate400
                )
            } else {
                appUsage.forEach { (appName, usageMinutes) ->
                    val widthFraction = (usageMinutes / 240f).toFloat().coerceIn(0f, 1f)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = appName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = Color.White)
                            Text(
                                text = "${usageMinutes / 60}h ${usageMinutes % 60}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate400
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(Slate900)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(widthFraction)
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(Indigo500)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WellbeingSection(stats: Stats, liveSteps: Int, hasPermission: Boolean, onAddWater: () -> Unit) {
    val context = LocalContext.current
    Column {
        Text(
            text = "Wellbeing",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Slate800.copy(alpha = 0.5f))
                .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Orange500.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.DirectionsWalk, contentDescription = null, tint = Orange400, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Steps", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, color = Color.White)
                        Text("Daily goal: 10k", style = MaterialTheme.typography.labelSmall, color = Slate400)
                    }
                }
                if (!hasPermission) {
                    Text(
                        text = "Permission Required",
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate400
                    )
                } else {
                    Text(
                        text = String.format(Locale.getDefault(), "%,d", liveSteps),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Blue500.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = Blue400, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Water", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, color = Color.White)
                        Text("Daily goal: 64 oz", style = MaterialTheme.typography.labelSmall, color = Slate400)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${stats.waterOunces} oz",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = onAddWater,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Blue500.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Add Water", tint = Blue400, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun UpNextSection(pendingTasks: List<Task>) {
    Column {
        Text(
            text = "Up Next",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        val displayTasks = pendingTasks.take(2)
        
        if (displayTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Slate800.copy(alpha = 0.4f))
                    .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("You are all caught up!", color = Slate400)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                displayTasks.forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Slate800.copy(alpha = 0.8f))
                            .border(1.dp, Slate700, RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Amber500)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
