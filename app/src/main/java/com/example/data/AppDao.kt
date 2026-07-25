package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.Note
import com.example.model.RoutineEvent
import com.example.model.Stats
import com.example.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: String)
    
    @Query("SELECT * FROM stats WHERE id = 1")
    fun getStats(): Flow<Stats?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: Stats)

    @Query("UPDATE stats SET tasksCompleted = MAX(0, tasksCompleted + :delta) WHERE id = 1")
    suspend fun updateTasksCompletedDelta(delta: Int)

    @Query("UPDATE stats SET focusHours = focusHours + :hours WHERE id = 1")
    suspend fun updateFocusHoursDelta(hours: Float)

    @Query("UPDATE stats SET waterOunces = waterOunces + :ounces WHERE id = 1")
    suspend fun updateWaterOuncesDelta(ounces: Int)

    @Query("SELECT * FROM notes ORDER BY pinned DESC, updatedAt DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: String)

    @Query("SELECT * FROM routines ORDER BY startTime ASC")
    fun getAllRoutines(): Flow<List<RoutineEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: RoutineEvent)

    @Query("DELETE FROM routines WHERE id = :routineId")
    suspend fun deleteRoutine(routineId: String)
}
