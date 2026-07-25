package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Square
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Stats
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun FocusScreen(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val stats by viewModel.stats.collectAsState()
    val currentStats = stats ?: Stats()
    
    var mode by remember { mutableStateOf("pomodoro") }
    var isActive by remember { mutableStateOf(false) }
    
    var pomodoroMinutes by remember { mutableIntStateOf(25) }
    var shortBreakMinutes by remember { mutableIntStateOf(5) }
    var longBreakMinutes by remember { mutableIntStateOf(15) }
    var showSettings by remember { mutableStateOf(false) }
    
    val timers = mapOf(
        "pomodoro" to pomodoroMinutes * 60,
        "shortBreak" to shortBreakMinutes * 60,
        "longBreak" to longBreakMinutes * 60
    )
    
    var timeLeft by remember(mode, pomodoroMinutes, shortBreakMinutes, longBreakMinutes) { mutableIntStateOf(timers[mode] ?: (25 * 60)) }
    
    LaunchedEffect(isActive, timeLeft) {
        if (isActive && timeLeft > 0) {
            delay(1000L)
            timeLeft -= 1
        } else if (isActive && timeLeft == 0) {
            isActive = false
            if (mode == "pomodoro") {
                viewModel.addFocusSession((timers["pomodoro"] ?: 0) / 3600f)
                mode = "shortBreak"
            } else {
                mode = "pomodoro"
            }
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp, bottom = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Focus",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Indigo500.copy(alpha = 0.1f))
                    .border(1.dp, Indigo500.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${String.format("%.1f", currentStats.focusHours)}h Today",
                    color = Indigo400,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Mode Selector
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .background(Slate800.copy(alpha = 0.8f))
                .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            ModeButton("Pomodoro", mode == "pomodoro") { 
                isActive = false
                mode = "pomodoro" 
            }
            ModeButton("Short Break", mode == "shortBreak") { 
                isActive = false
                mode = "shortBreak" 
            }
            ModeButton("Long Break", mode == "longBreak") { 
                isActive = false
                mode = "longBreak" 
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Circular Timer
        val totalTime = timers[mode] ?: 1
        val progress = (totalTime - timeLeft).toFloat() / totalTime.toFloat()
        
        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            animationSpec = tween(1000, easing = LinearEasing),
            label = "progress"
        )
        
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(280.dp)
        ) {
            val strokeColor = if (mode == "pomodoro") Indigo500 else Emerald500
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 8.dp.toPx()
                drawArc(
                    color = Slate800,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round),
                    size = Size(size.width, size.height)
                )
                
                drawArc(
                    color = strokeColor,
                    startAngle = -90f,
                    sweepAngle = 360f * (1f - animatedProgress),
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Round),
                    size = Size(size.width, size.height)
                )
            }
            
            val minutes = timeLeft / 60
            val seconds = timeLeft % 60
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                fontSize = 64.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                letterSpacing = (-1).sp
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            IconButton(
                onClick = { 
                    isActive = false
                    timeLeft = timers[mode] ?: 0
                },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Slate800)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Slate400)
            }
            
            IconButton(
                onClick = { isActive = !isActive },
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(if (isActive) Rose500 else Indigo600)
            ) {
                if (isActive) {
                    Icon(Icons.Default.Square, contentDescription = "Pause", tint = Color.White, modifier = Modifier.size(32.dp))
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(40.dp))
                }
            }
            
            IconButton(
                onClick = { showSettings = true },
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Slate800)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Slate400)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Timer Settings", color = Color.White) },
            text = {
                Column {
                    Text("Pomodoro (minutes)", color = Slate300)
                    Slider(
                        value = pomodoroMinutes.toFloat(),
                        onValueChange = { pomodoroMinutes = it.toInt() },
                        valueRange = 1f..60f,
                        steps = 59
                    )
                    Text("${pomodoroMinutes}m", color = Color.White)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Short Break (minutes)", color = Slate300)
                    Slider(
                        value = shortBreakMinutes.toFloat(),
                        onValueChange = { shortBreakMinutes = it.toInt() },
                        valueRange = 1f..30f,
                        steps = 29
                    )
                    Text("${shortBreakMinutes}m", color = Color.White)
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettings = false }) {
                    Text("Done", color = Indigo400)
                }
            },
            containerColor = Slate900,
            titleContentColor = Color.White,
            textContentColor = Slate300
        )
    }
}

@Composable
private fun ModeButton(text: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (active) Slate700 else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (active) Color.White else Slate400,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}
