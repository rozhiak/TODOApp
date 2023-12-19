package com.rmblack.todoapp.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.models.local.Task
import kotlinx.coroutines.launch

class SharedTasksViewModel(application: Application) :
    TasksViewModel(application) {
    init {
        viewModelScope.launch {
            taskRepository.getSharedTasksFlow().collect { tasks ->
                updateTasks(tasks)
            }
        }
    }
}