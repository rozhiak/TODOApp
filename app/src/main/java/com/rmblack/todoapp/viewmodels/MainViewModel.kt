package com.rmblack.todoapp.viewmodels

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.fragments.PrivateTasksFragment
import com.rmblack.todoapp.fragments.SharedTasksFragment
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.models.server.requests.UpdateUserRequest
import com.rmblack.todoapp.models.server.success.User
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.utils.Utilities
import com.rmblack.todoapp.utils.Utilities.SharedObject.setSyncingState
import com.rmblack.todoapp.webservice.repository.ApiRepository
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.Result.Companion.failure

const val SAME_USER_NAME_CODE = 1

class MainViewModel(val sharedPreferencesManager: SharedPreferencesManager) : ViewModel() {

    val privateTasksFragment: Fragment = PrivateTasksFragment()

    val sharedTasksFragment: Fragment = SharedTasksFragment()

    private val taskRepository = TaskRepository.get()

    private val apiRepository = ApiRepository()

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

    suspend fun updateUserInServer(newName: String): Result<Int> {
        val user = sharedPreferencesManager.getUser()
        if (user?.name != newName) {
            val updateUserRequest = UpdateUserRequest(
                user?.token ?: "", newName
            )
            if (!user?.token.isNullOrBlank()) {
                try {
                    val response = apiRepository.updateUser(updateUserRequest)
                    when (response.code()) {
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
        return failure(UpdateUserException(SAME_USER_NAME_CODE))
    }

    fun getUserFromSharedPreferences(): User? {
        return sharedPreferencesManager.getUser()
    }

    fun getEntranceState(): Boolean {
        return sharedPreferencesManager.getEntranceState()
    }

    private fun updateUserName(newName: String) {
        val user = sharedPreferencesManager.getUser()?.copy(
            name = newName
        )
        if (user != null) {
            sharedPreferencesManager.saveUser(
                user
            )
        }
    }

    fun syncTasksWithServer() {
        val user = getUserFromSharedPreferences()
        if (user != null) {
            viewModelScope.launch {
                val res = Utilities.syncTasksWithServer(user.token, sharedPreferencesManager)
                res.onSuccess {
                    setSyncingState(false)
                }
                res.onFailure {
                    setSyncingState(false)
                }
            }
        }
    }

    class UpdateUserException(val intValue: Int) : Exception()
}