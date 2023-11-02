package com.rmblack.todoapp.viewmodels

import android.content.Context
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
import com.rmblack.todoapp.webservice.repository.ApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.Result.Companion.failure

const val SAME_USER_NAME = 1

class MainViewModel(val sharedPreferencesManager: SharedPreferencesManager) : ViewModel() {

    val privateTasksFragment: Fragment = PrivateTasksFragment()

    val sharedTasksFragment: Fragment = SharedTasksFragment()

    private val taskRepository = TaskRepository.get()

    private val apiRepository = ApiRepository()

    private val _isSyncing = MutableStateFlow(false)

    val isSyncing get() = _isSyncing.asStateFlow()

    fun updateSyncState(state: Boolean) {
        _isSyncing.update {
            state
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

    suspend fun updateUserInServer(newName: String): Result<Int> {
        val user = sharedPreferencesManager.getUser()
        if (user?.name != newName) {
            val updateUserRequest = UpdateUserRequest(
                user?.token ?: "",
                newName
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
        return failure(UpdateUserException(SAME_USER_NAME))
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

    fun syncTasksWithServer(context: Context) {
        val user = getUserFromSharedPreferences()
        if (user != null) {
            viewModelScope.launch {
                val res = Utilities.syncTasksWithServer(user.token, context)
                res.onSuccess {
                    updateSyncState(false)
                }
                res.onFailure {
                    updateSyncState(false)
                }
            }
        }
    }

    class UpdateUserException(val intValue: Int) : Exception()
}