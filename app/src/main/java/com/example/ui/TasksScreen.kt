package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Task
import com.example.ui.theme.*

@Composable
fun TasksScreen(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val tasks by viewModel.tasks.collectAsState()
    val pending = tasks.filter { !it.completed }
    val completed = tasks.filter { it.completed }
    
    var showAddTask by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Tasks",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            
            item {
                AnimatedVisibility(
                    visible = showAddTask,
                    enter = expandVertically(animationSpec = tween(300)),
                    exit = shrinkVertically(animationSpec = tween(300))
                ) {
                    AddTaskForm(
                        onAdd = { title, priority, category, recurrence ->
                            viewModel.addTask(title, priority, category, recurrence)
                            showAddTask = false
                        },
                        onCancel = { showAddTask = false }
                    )
                }
            }
            
            item {
                Text(
                    text = "PENDING",
                    style = MaterialTheme.typography.labelMedium,
                    color = Slate400,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                if (pending.isEmpty()) {
                    Text("No pending tasks.", color = Slate500, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        pending.forEach { task ->
                            TaskItem(
                                task = task,
                                onToggle = { viewModel.toggleTask(task) },
                                onDelete = { viewModel.deleteTask(task.id) }
                            )
                        }
                    }
                }
            }
            
            if (completed.isNotEmpty()) {
                item {
                    Text(
                        text = "COMPLETED",
                        style = MaterialTheme.typography.labelMedium,
                        color = Slate400,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 100.dp)
                    ) {
                        completed.forEach { task ->
                            TaskItem(
                                task = task,
                                onToggle = { viewModel.toggleTask(task) },
                                onDelete = { viewModel.deleteTask(task.id) }
                            )
                        }
                    }
                }
            } else {
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
        
        FloatingActionButton(
            onClick = { showAddTask = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .padding(bottom = 80.dp),
            containerColor = Indigo600,
            contentColor = Color.White,
            shape = CircleShape
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Task")
        }
    }
}

val Indigo600 = Color(0xFF4F46E5)

@Composable
private fun AddTaskForm(
    onAdd: (String, String, String, String?) -> Unit,
    onCancel: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var category by remember { mutableStateOf("Personal") }
    
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
                if (title.isEmpty()) {
                    Text("What needs to be done?", color = Slate500, style = MaterialTheme.typography.titleLarge)
                }
                innerTextField()
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (title.isNotBlank()) onAdd(title.trim(), priority, category, null)
            })
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = priority == "High",
                onClick = { priority = if (priority == "High") "Medium" else "High" },
                label = { Text("High") },
                leadingIcon = { Icon(Icons.Outlined.Flag, null, modifier = Modifier.size(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Rose500.copy(alpha = 0.2f),
                    selectedLabelColor = Rose400,
                    selectedLeadingIconColor = Rose400
                )
            )
            
            val categories = listOf("Personal", "Work", "Study", "Health", "Other")
            
            FilterChip(
                selected = true,
                onClick = { 
                    val nextIndex = (categories.indexOf(category) + 1) % categories.size
                    category = categories[nextIndex]
                },
                label = { Text(category) },
                leadingIcon = { Icon(Icons.Outlined.Sell, null, modifier = Modifier.size(16.dp)) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Slate900.copy(alpha = 0.5f),
                    selectedLabelColor = Slate300,
                    selectedLeadingIconColor = Slate400
                )
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text("Cancel", color = Slate400)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { if (title.isNotBlank()) onAdd(title.trim(), priority, category, null) },
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
            ) {
                Text("Save Task")
            }
        }
    }
}

@Composable
private fun TaskItem(task: Task, onToggle: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Slate800.copy(alpha = 0.5f))
            .border(1.dp, Slate700.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onToggle,
            modifier = Modifier.size(28.dp)
        ) {
            if (task.completed) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Emerald500)
            } else {
                Icon(Icons.Outlined.Circle, contentDescription = null, tint = Slate400)
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                color = if (task.completed) Slate400 else Color.White,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (task.completed) TextDecoration.LineThrough else null
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val priorityColor = when (task.priority) {
                    "High" -> Rose400
                    "Medium" -> Amber400
                    else -> Blue400
                }
                val priorityBg = when (task.priority) {
                    "High" -> Rose500.copy(alpha = 0.2f)
                    "Medium" -> Amber500.copy(alpha = 0.2f)
                    else -> Blue500.copy(alpha = 0.2f)
                }
                
                Text(
                    text = task.priority,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = priorityColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(priorityBg)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
                
                Text(
                    text = task.category,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate300,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate700)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Slate500)
        }
    }
}
