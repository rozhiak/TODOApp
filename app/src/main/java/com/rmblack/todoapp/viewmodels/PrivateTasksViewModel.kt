package com.rmblack.todoapp.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class PrivateTasksViewModel(application: Application) : TasksViewModel(application) {

    private val privateTasksFlow = taskRepository.getPrivateTasksFlow()

    init {
        viewModelScope.launch {
            privateTasksFlow.collect { tasks ->
                updateTasks(tasks)
            }
        }
    }
}