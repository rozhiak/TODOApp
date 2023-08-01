package com.rmblack.todoapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rmblack.todoapp.models.Task
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
    fun updateTask(task: Task)

    @Query("UPDATE task SET isDone=:isDone WHERE id = :id")
    fun updateDoneState(isDone: Boolean, id: UUID)

    @Query("UPDATE task SET isUrgent=:isUrgent WHERE id = :id")
    fun updateUrgentState(isUrgent: Boolean, id: UUID)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(task: Task)
}