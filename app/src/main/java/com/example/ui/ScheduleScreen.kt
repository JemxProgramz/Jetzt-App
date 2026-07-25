package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RoutineEvent
import com.example.ui.theme.*

val ROUTINE_COLORS = listOf(Indigo500, Rose500, Emerald500, Amber500, Blue500, Orange500)
val DAYS = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")

@Composable
fun ScheduleScreen(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val routines by viewModel.routines.collectAsState()
    
    var selectedDay by remember { mutableStateOf("Monday") }
    var isAdding by remember { mutableStateOf(false) }

    val daysRoutines = routines
        .filter { it.dayOfWeek == selectedDay }
        .sortedBy { it.startTime }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp)
        ) {
            Text(
                text = "Weekly Schedule",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = "Manage your Monday-Friday routines",
                style = MaterialTheme.typography.bodyMedium,
                color = Slate400,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DAYS.forEach { day ->
                    val isSelected = day == selectedDay
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Indigo600 else Slate800.copy(alpha = 0.5f))
                            .clickable { selectedDay = day }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.take(3),
                            color = if (isSelected) Color.White else Slate300,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(visible = isAdding) {
                AddRoutineForm(
                    selectedDay = selectedDay,
                    onSave = { title, start, end, color ->
                        viewModel.addRoutine(title, selectedDay, start, end, color)
                        isAdding = false
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "${selectedDay}'s Routine".uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Slate400,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (daysRoutines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Slate800.copy(alpha = 0.3f))
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No routines set for $selectedDay.", color = Slate500)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(daysRoutines) { routine ->
                        RoutineItem(routine = routine, onDelete = { viewModel.deleteRoutine(routine.id) })
                    }
                }
            }
        }
        
        FloatingActionButton(
            onClick = { isAdding = !isAdding },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .padding(bottom = 80.dp),
            containerColor = Indigo600,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Routine")
        }
    }
}

@Composable
private fun AddRoutineForm(
    selectedDay: String,
    onSave: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var selectedColor by remember { mutableStateOf(ROUTINE_COLORS[0]) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Slate800)
            .border(1.dp, Slate700, RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BasicTextField(
            value = title,
            onValueChange = { title = it },
            textStyle = MaterialTheme.typography.titleLarge.copy(color = Color.White),
            cursorBrush = SolidColor(Indigo500),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (title.isEmpty()) Text("Routine Activity", color = Slate500, style = MaterialTheme.typography.titleLarge)
                innerTextField()
            }
        )
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TimeInput(label = "START TIME", time = startTime, onTimeChange = { startTime = it }, modifier = Modifier.weight(1f))
            TimeInput(label = "END TIME", time = endTime, onTimeChange = { endTime = it }, modifier = Modifier.weight(1f))
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.Palette, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
                ROUTINE_COLORS.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { selectedColor = color }
                            .border(
                                width = 2.dp,
                                color = if (selectedColor == color) Color.White.copy(alpha = 0.5f) else Color.Transparent,
                                shape = CircleShape
                            )
                    )
                }
            }
            
            Button(
                onClick = { if (title.isNotBlank()) onSave(title, startTime, endTime, selectedColor.value.toString()) },
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun TimeInput(label: String, time: String, onTimeChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Slate900.copy(alpha = 0.5f))
            .border(1.dp, Slate700, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Text(text = label, color = Slate400, fontSize = 10.sp, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 4.dp))
        // Simple text field for time for now
        BasicTextField(
            value = time,
            onValueChange = onTimeChange,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Slate200),
            cursorBrush = SolidColor(Indigo500),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun RoutineItem(routine: RoutineEvent, onDelete: () -> Unit) {
    val color = runCatching { Color(routine.color.toULong()) }.getOrDefault(Indigo500)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Slate800.copy(alpha = 0.8f))
            .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(6.dp)
                .height(72.dp)
                .background(color)
        )
        
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .width(60.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = routine.startTime,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = Slate400
            )
        }
        
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(40.dp)
                .background(Slate700.copy(alpha = 0.5f))
        )
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = routine.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Schedule, contentDescription = null, tint = Slate500, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${routine.startTime} - ${routine.endTime}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate500
                )
            }
        }
        
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Slate500)
        }
    }
}
