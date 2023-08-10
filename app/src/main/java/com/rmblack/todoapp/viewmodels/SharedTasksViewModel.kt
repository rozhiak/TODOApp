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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class SharedTasksViewModel: ViewModel() {

    val taskRepository = TaskRepository.get()

    private var _sharedTasks : MutableStateFlow<List<Task>> = MutableStateFlow(emptyList())

    val sharedTasks
        get() = _sharedTasks.asStateFlow()

    private val _detailsVisibility: ArrayList<Boolean> = ArrayList()

    val detailsVisibility: List<Boolean>
        get() = _detailsVisibility.toList()

    init {
        viewModelScope.launch {
            taskRepository.getSharedTasks().collect {
                _sharedTasks.value = it

                while (_detailsVisibility.size < it.size) {
                    _detailsVisibility.add(false)
                }
                while (_detailsVisibility.size > it.size) {
                    _detailsVisibility.removeAt(_detailsVisibility.size - 1)
                }
            }
        }
    }

    fun updateTasks(onUpdate: (List<Task>) -> (List<Task>)) {
        _sharedTasks.update {
            onUpdate(it)
        }
    }

    fun updateUrgentState(isUrgent: Boolean, id: UUID, pos: Int) {
        updateTasks { oldTasks ->
            val updatedTasks = oldTasks.toMutableList()
            updatedTasks[pos] = sharedTasks.value[pos].copy(isUrgent = isUrgent)
            updatedTasks
        }

        taskRepository.updateUrgentState(isUrgent, id)
    }

    fun updateDoneState(isDone: Boolean, id: UUID, pos: Int) {
        updateTasks { oldTasks ->
            val updatedTasks = oldTasks.toMutableList()
            updatedTasks[pos] = sharedTasks.value[pos].copy(isDone = isDone)
            updatedTasks
        }

        taskRepository.updateDoneState(isDone, id)
    }

    fun updateVisibility(index: Int, visibility: Boolean) {
        if (index < _detailsVisibility.size) _detailsVisibility[index] = visibility
    }
}