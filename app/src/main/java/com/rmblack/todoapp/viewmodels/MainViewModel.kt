package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.ViewModel
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.models.server.success.User
import com.rmblack.todoapp.utils.SharedPreferencesManager
import com.rmblack.todoapp.webservice.repository.ApiRepository

class MainViewModel(private val sharedPreferencesManager: SharedPreferencesManager): ViewModel() {

    private val taskRepository = TaskRepository.get()

    suspend fun addTask(task: Task) {
        taskRepository.addTask(task)
    }

    fun getUserFromSharedPreferences(): User? {
        return sharedPreferencesManager.getUser()
    }

    fun getEntranceState(): Boolean {
        return sharedPreferencesManager.getEntranceState()
    }
}