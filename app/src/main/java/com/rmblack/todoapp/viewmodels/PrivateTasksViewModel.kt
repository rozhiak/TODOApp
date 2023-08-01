package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.Task
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class PrivateTasksViewModel: ViewModel() {

    private val taskRepository = TaskRepository.get()

    private val _privateTasks: MutableStateFlow<List<Task>> = MutableStateFlow(emptyList())

    val privateTasks: StateFlow<List<Task>>
        get() = _privateTasks.asStateFlow()

    init {
        viewModelScope.launch {
            taskRepository.getPrivateTasks().collect {
                _privateTasks.value = it
            }
        }
    }

    fun updateTask(onUpdate: (List<Task>) -> List<Task>) {
        _privateTasks.update { oldTasks ->
            onUpdate(oldTasks)
        }
    }

    fun updateUrgentState(isUrgent: Boolean, id: UUID) {
        taskRepository.updateUrgentState(isUrgent, id)
    }

    fun updateDoneState(isDone: Boolean, id: UUID) {
        taskRepository.updateDoneState(isDone, id)
    }
}