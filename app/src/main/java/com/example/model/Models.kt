package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val completed: Boolean = false,
    val priority: String = "Medium",
    val category: String = "Personal",
    val recurrence: String? = null
)

@Entity(tableName = "stats")
data class Stats(
    @PrimaryKey val id: Int = 1,
    val focusHours: Float = 0f,
    val stepsToday: Int = 0,
    val waterOunces: Int = 0,
    val tasksCompleted: Int = 0,
    val currentStreak: Int = 0,
    val lastActiveDate: Long = 0L
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val pinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "routines")
data class RoutineEvent(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val color: String
)
