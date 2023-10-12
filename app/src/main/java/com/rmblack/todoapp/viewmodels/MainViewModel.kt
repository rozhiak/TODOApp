package com.rmblack.todoapp.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.models.server.requests.UpdateUserRequest
import com.rmblack.todoapp.models.server.success.User
import com.rmblack.todoapp.utils.CONNECTION_ERROR_CODE
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.utils.Utilities
import com.rmblack.todoapp.webservice.repository.ApiRepository
import kotlinx.coroutines.launch
import okhttp3.internal.userAgent
import retrofit2.Response
import java.lang.Exception
import java.net.UnknownHostException
import kotlin.Result.Companion.failure

const val SAME_USER_NAME = 1

class MainViewModel(private val sharedPreferencesManager: SharedPreferencesManager): ViewModel() {

    private val taskRepository = TaskRepository.get()

    private val apiRepository = ApiRepository()

    fun collapseExpandedTask() {
        viewModelScope.launch {
            val tasks = taskRepository.getTasks()
            for (t in tasks) {
                if (t.detailsVisibility) {
                    taskRepository.updateDetailsVisibility(t.id, false)
                }
            }
        }
    }

    fun removeNoTitleTasks() {
        viewModelScope.launch {
            val tasks = taskRepository.getTasks()
            for (t in tasks) {
                if (t.title == "") {
                    taskRepository.deleteTask(t)
                }
            }
        }
    }

    suspend fun addTask(task: Task) {
        taskRepository.addTask(task)
    }

    suspend fun updateUser(newName: String): Result<Int> {
        val user = sharedPreferencesManager.getUser()
        if (user?.name != newName) {
            val updateUserRequest = UpdateUserRequest(
                user?.token ?: "",
                newName
            )
            if (!user?.token.isNullOrBlank()){
                try {
                    val response = apiRepository.updateUser(updateUserRequest)
                    when(response.code()) {
                        200 -> {
                            updateUserName(newName)
                            return Result.success(200)
                        }
                        404 -> {
                            return failure(UpdateUserException(404))
                        }
                    }
                } catch (e: Exception) {
                    return failure(e)
                }
            } else {
                return failure(UpdateUserException(404))
            }
        }
        return failure(UpdateUserException(SAME_USER_NAME))
    }

    fun getUserFromSharedPreferences(): User? {
        return sharedPreferencesManager.getUser()
    }

    fun getEntranceState(): Boolean {
        return sharedPreferencesManager.getEntranceState()
    }

    private fun updateUserName(newName : String) {
        val user = sharedPreferencesManager.getUser()?.copy(
            name = newName
        )
        if (user != null) {
            sharedPreferencesManager.saveUser(
                user
            )
        }
    }

    fun syncTasksWithServer(context: Context) {
        val user = getUserFromSharedPreferences()
        if (user != null) {
            viewModelScope.launch {
                Utilities.syncTasksWithServer(user.token, context)
            }
        }
    }

    class UpdateUserException(val intValue: Int) : Exception()
}