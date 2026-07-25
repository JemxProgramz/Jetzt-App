package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.model.Note
import com.example.model.RoutineEvent
import com.example.model.Stats
import com.example.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).appDao()

    val tasks: StateFlow<List<Task>> = dao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stats: StateFlow<Stats?> = dao.getStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val notes: StateFlow<List<Note>> = dao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    val routines: StateFlow<List<RoutineEvent>> = dao.getAllRoutines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _appUsage = MutableStateFlow<List<Pair<String, Long>>>(emptyList())
    val appUsage: StateFlow<List<Pair<String, Long>>> = _appUsage.asStateFlow()

    private val _liveSteps = MutableStateFlow(0)
    val liveSteps: StateFlow<Int> = _liveSteps.asStateFlow()

    private val _hasUsagePermission = MutableStateFlow(false)
    val hasUsagePermission: StateFlow<Boolean> = _hasUsagePermission.asStateFlow()

    private val _hasActivityPermission = MutableStateFlow(false)
    val hasActivityPermission: StateFlow<Boolean> = _hasActivityPermission.asStateFlow()

    fun updateUsagePermissionState(hasPermission: Boolean) {
        _hasUsagePermission.value = hasPermission
    }

    fun updateActivityPermissionState(hasPermission: Boolean) {
        _hasActivityPermission.value = hasPermission
        if (hasPermission) {
            viewModelScope.launch {
                StepCounterUtils.getStepCountFlow(getApplication()).collect { steps ->
                    _liveSteps.value = steps
                }
            }
        }
    }

    fun fetchAppUsage() {
        if (_hasUsagePermission.value) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                _appUsage.value = AppUsageUtils.getDailyAppUsage(getApplication())
            }
        }
    }

    init {
        // Initialize stats if empty and check streak
        viewModelScope.launch {
            val currentStats = dao.getStats().first()
            if (currentStats == null) {
                dao.insertStats(Stats(currentStreak = 1, focusHours = 2.5f, stepsToday = 8432, waterOunces = 48, tasksCompleted = 3, lastActiveDate = System.currentTimeMillis()))
            } else {
                checkStreak(currentStats)
            }
        }
    }

    private suspend fun checkStreak(currentStats: Stats) {
        val today = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val todayTime = today.timeInMillis

        val lastActive = java.util.Calendar.getInstance().apply {
            timeInMillis = currentStats.lastActiveDate
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val lastActiveTime = lastActive.timeInMillis

        val oneDay = 24 * 60 * 60 * 1000L

        if (currentStats.lastActiveDate == 0L) {
            dao.insertStats(currentStats.copy(currentStreak = 1, lastActiveDate = todayTime))
        } else if (todayTime - lastActiveTime == oneDay) {
            dao.insertStats(currentStats.copy(currentStreak = currentStats.currentStreak + 1, lastActiveDate = todayTime))
        } else if (todayTime - lastActiveTime > oneDay) {
            dao.insertStats(currentStats.copy(currentStreak = 1, lastActiveDate = todayTime))
        }
    }

    fun addTask(title: String, priority: String, category: String, recurrence: String?) {
        viewModelScope.launch {
            dao.insertTask(
                Task(
                    title = title,
                    priority = priority,
                    category = category,
                    recurrence = recurrence
                )
            )
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            val updatedTask = task.copy(completed = !task.completed)
            dao.updateTask(updatedTask)
            
            // Update stats
            val currentStats = stats.value ?: Stats()
            val newCompletedCount = if (updatedTask.completed) {
                currentStats.tasksCompleted + 1
            } else {
                (currentStats.tasksCompleted - 1).coerceAtLeast(0)
            }
            dao.insertStats(currentStats.copy(tasksCompleted = newCompletedCount))
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            dao.deleteTask(taskId)
        }
    }

    fun addFocusSession(hours: Float) {
        viewModelScope.launch {
            val currentStats = stats.value ?: Stats()
            dao.insertStats(currentStats.copy(focusHours = currentStats.focusHours + hours))
        }
    }

    fun addNote(title: String, content: String, pinned: Boolean = false) {
        viewModelScope.launch {
            dao.insertNote(Note(title = title, content = content, pinned = pinned))
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            dao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            dao.deleteNote(noteId)
        }
    }

    fun addRoutine(title: String, dayOfWeek: String, startTime: String, endTime: String, color: String) {
        viewModelScope.launch {
            dao.insertRoutine(
                RoutineEvent(
                    title = title,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    color = color
                )
            )
        }
    }

    fun deleteRoutine(routineId: String) {
        viewModelScope.launch {
            dao.deleteRoutine(routineId)
        }
    }
}
