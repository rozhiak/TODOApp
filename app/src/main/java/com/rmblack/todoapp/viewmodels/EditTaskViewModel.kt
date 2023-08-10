package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class EditTaskViewModel(taskId: UUID): ViewModel() {
    private val taskRepository = TaskRepository.get()

    private val _task : MutableStateFlow<Task?> = MutableStateFlow(null)
    val task : StateFlow<Task?> = _task.asStateFlow()

    init {
        viewModelScope.launch {
            val task = withContext(Dispatchers.IO) {
                taskRepository.getTask(taskId)
            }
            _task.value = task
        }

    }

    fun updateTask(onUpdate: (Task) -> Task) {
        _task.update {oldTask ->
            oldTask?.let {
                onUpdate(it)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        task.value?.let {
            taskRepository.updateTask(it)
        }
    }
}

class EditTaskViewModelFactory(
    private val taskId: UUID
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditTaskViewModel(taskId) as T
    }
}