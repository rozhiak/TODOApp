package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PrivateTasksViewModel: ViewModel() {

    private val taskRepository = TaskRepository.get()

    private val _privateTasks: MutableStateFlow<List<Task>> = MutableStateFlow(emptyList())

    val privateTasks
        get() = _privateTasks.asStateFlow()

    init {
        viewModelScope.launch {
            taskRepository.getPrivateTasks().collect {
                _privateTasks.value = it
            }
        }
    }
}