package com.rmblack.todoapp.data.repository

import android.content.Context
import androidx.room.Room
import com.rmblack.todoapp.database.TaskDatabase
import com.rmblack.todoapp.models.Task
import kotlinx.coroutines.flow.Flow
import java.lang.IllegalStateException
import java.util.UUID

private const val DATABASE_NAME ="crime-database"

class TaskRepository private constructor(context: Context) {

    private val database: TaskDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            TaskDatabase::class.java,
            DATABASE_NAME
        )
        .build()

    fun getTasks(): Flow<List<Task>> = database.taskDao().getTasks()

    fun getPrivateTasks(): Flow<List<Task>> = database.taskDao().getPrivateTasks()

    fun getSharedTasks(): Flow<List<Task>> = database.taskDao().getSharedTasks()



    fun getTask(id: UUID): Task = database.taskDao().getTask(id)

    fun insert(task: Task) = database.taskDao().insert(task)

    companion object {
        private var INSTANCE: TaskRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = TaskRepository(context)
            }
        }

        fun get(): TaskRepository {
            return INSTANCE ?:
            throw IllegalStateException("TaskRepository must be initialized")
        }
    }

}