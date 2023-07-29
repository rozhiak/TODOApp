package com.rmblack.todoapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rmblack.todoapp.models.Task
import java.util.UUID

@Dao
interface TaskDao {
    @Query("SELECT * FROM task")
    fun getTasks(): List<Task>

    @Query("SELECT * FROM Task WHERE id=(:id)")
    fun getTask(id: UUID): Task

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(task: Task)
}