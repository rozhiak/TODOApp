package com.rmblack.todoapp.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rmblack.todoapp.models.local.Task
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface TaskDao {
    @Query("SELECT * FROM task")
    suspend fun getTasks(): List<Task>

    @Query("SELECT * FROM task WHERE isShared = 0")
    fun getPrivateTasksFlow(): Flow<List<Task>>

    @Query("SELECT * FROM task WHERE isShared = 1")
    fun getSharedTasksFlow(): Flow<List<Task>>

    @Query("SELECT * FROM task WHERE isShared = 0")
    suspend fun getPrivateTasks(): List<Task>

    @Query("SELECT * FROM task WHERE isShared = 1")
    suspend fun getSharedTasks(): List<Task>

    @Query("SELECT * FROM Task WHERE id=(:id)")
    fun getTask(id: UUID): Task

    @Query("UPDATE task SET detailsVisibility = :isVisible WHERE id = :id")
    suspend fun updateDetailsVisibility(isVisible: Boolean, id: UUID)

    @Update
    suspend fun updateTask(task: Task)

    @Query("UPDATE task SET isDone = :isDone WHERE id = :id")
    suspend fun updateDoneState(isDone: Boolean, id: UUID)

    @Query("UPDATE task SET isUrgent = :isUrgent WHERE id = :id")
    suspend fun updateUrgentState(isUrgent: Boolean, id: UUID)

    @Query("UPDATE task SET serverID = :serverID WHERE id = :id")
    suspend fun updateServerID(id: UUID, serverID: String)

    @Query("UPDATE task SET alarm = :alarm WHERE id = :id")
    suspend fun updateAlarm(id: UUID, alarm: Boolean)

    @Query("DELETE FROM task WHERE isShared = 1")
    suspend fun deleteSharedTasks()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Delete
    suspend fun delete(task: Task?)
}