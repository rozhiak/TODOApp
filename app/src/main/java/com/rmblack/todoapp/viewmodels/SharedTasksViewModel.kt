package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aminography.primecalendar.persian.PersianCalendar
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.Task
import com.rmblack.todoapp.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.UUID

class SharedTasksViewModel: ViewModel() {

    val taskRepository = TaskRepository.get()

    private var _sharedTasks : MutableStateFlow<List<Task>> = MutableStateFlow(emptyList())

    val sharedTasks
        get() = _sharedTasks.asStateFlow()

    init {
        viewModelScope.launch {
            taskRepository.getSharedTasks().collect {
                _sharedTasks.value = it
            }
        }
    }

}