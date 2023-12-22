package com.rmblack.todoapp.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.models.local.Task
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SharedTasksViewModel(application: Application) : TasksViewModel(application) {

    private val sharedTasksFlow = taskRepository.getSharedTasksFlow()

    init {
        viewModelScope.launch {
            sharedTasksFlow.collect {tasks ->
                updateTasks(tasks)
            }
        }
    }
}