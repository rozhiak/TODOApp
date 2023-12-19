package com.rmblack.todoapp.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.models.local.Task
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PrivateTasksViewModel(application: Application) :
    TasksViewModel(application) {
    init {
        viewModelScope.launch {
            taskRepository.getPrivateTasksFlow().collect { tasks ->
                updateTasks(tasks)
            }
        }
    }
}