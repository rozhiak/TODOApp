package com.rmblack.todoapp.data.repository

import android.content.Context
import androidx.room.Room
import com.rmblack.todoapp.data.database.TaskDatabase
import com.rmblack.todoapp.models.local.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import java.util.UUID

private const val DATABASE_NAME ="crime-database"

class TaskRepository private constructor(
    context: Context,
    private val coroutineScope: CoroutineScope = GlobalScope) {

    private val database: TaskDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            TaskDatabase::class.java,
            DATABASE_NAME
        )
        .build()

    suspend fun getTasks(): List<Task> = database.taskDao().getTasks()

    fun getTask(id: UUID) = database.taskDao().getTask(id)

    fun getPrivateTasksFlow(): Flow<List<Task>> = database.taskDao().getPrivateTasksFlow()

    fun getSharedTasksFlow(): Flow<List<Task>> = database.taskDao().getSharedTasksFlow()

    suspend fun getPrivateTasks(): List<Task> = database.taskDao().getPrivateTasks()

    suspend fun getSharedTasks(): List<Task> = database.taskDao().getSharedTasks()




    fun updateTask(task: Task) {
        coroutineScope.launch {
            database.taskDao().updateTask(task)
        }
    }

    fun updateDoneState(isDone: Boolean, id: UUID) {
        coroutineScope.launch {
            database.taskDao().updateDoneState(isDone, id)
        }
    }

    fun updateUrgentState(isUrgent: Boolean, id: UUID) {
        coroutineScope.launch() {
            database.taskDao().updateUrgentState(isUrgent, id)
        }
    }

    fun deleteTask(task: Task?) {
        coroutineScope.launch {
            database.taskDao().delete(task)
        }
    }

    fun deleteSharedTasks() {
        coroutineScope.launch {
            database.taskDao().deleteSharedTasks()
        }
    }

    fun updateServerID(id: UUID, serverID: String) {
        coroutineScope.launch {
            database.taskDao().updateServerID(id, serverID)
        }
    }

    fun updateDetailsVisibility(id: UUID, isVisible: Boolean) {
        coroutineScope.launch {
            database.taskDao().updateDetailsVisibility(isVisible, id)
        }
    }

    suspend fun addTask(task: Task) = database.taskDao().insert(task)

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