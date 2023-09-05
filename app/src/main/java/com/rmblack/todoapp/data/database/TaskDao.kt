package com.rmblack.todoapp.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.models.local.TaskState
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface TaskDao {
    @Query("SELECT * FROM task")
    fun getTasks(): Flow<List<Task>>

    @Query("SELECT * FROM task WHERE isShared = 0")
    fun getPrivateTasks(): Flow<List<Task>>

    @Query("SELECT * FROM task WHERE isShared = 1")
    fun getSharedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM Task WHERE id=(:id)")
    fun getTask(id: UUID): Task

    @Update
    suspend fun updateTask(task: Task)

    @Query("UPDATE task SET isDone = :isDone WHERE id = :id")
    suspend fun updateDoneState(isDone: Boolean, id: UUID)

    @Query("UPDATE task SET isUrgent = :isUrgent WHERE id = :id")
    suspend fun updateUrgentState(isUrgent: Boolean, id: UUID)

    @Query("UPDATE task SET state = :state WHERE id = :id")
    suspend fun updateTaskState(state: TaskState, id: UUID)

    @Query("UPDATE task SET serverID = :serverID WHERE id = :id")
    suspend fun updateServerID(id: UUID, serverID: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Delete
    suspend fun delete(task: Task?)
}