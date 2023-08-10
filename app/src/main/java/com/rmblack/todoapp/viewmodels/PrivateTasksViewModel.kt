package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.collections.ArrayList

class PrivateTasksViewModel: ViewModel() {

    private val taskRepository = TaskRepository.get()

    private val _privateTasks: MutableStateFlow<List<Task>> = MutableStateFlow(emptyList())

    val privateTasks: StateFlow<List<Task>>
        get() = _privateTasks.asStateFlow()

    private val _detailsVisibility: ArrayList<Boolean> = ArrayList()

    val detailsVisibility: List<Boolean>
        get() = _detailsVisibility.toList()

    init {
        viewModelScope.launch {
            taskRepository.getPrivateTasks().collect {
                _privateTasks.value = it

                while (_detailsVisibility.size < it.size) {
                    _detailsVisibility.add(false)
                }
                while (_detailsVisibility.size > it.size) {
                    _detailsVisibility.removeAt(_detailsVisibility.size - 1)
                }
            }
        }
    }

    private fun updateTasks(onUpdate: (List<Task>) -> List<Task>) {
        _privateTasks.update { oldTasks ->
            onUpdate(oldTasks)
        }
    }

    fun updateUrgentState(isUrgent: Boolean, id: UUID, pos: Int) {
        taskRepository.updateUrgentState(isUrgent, id)

        updateTasks { oldTasks ->
            val updatedTasks = oldTasks.toMutableList()
            updatedTasks[pos] = privateTasks.value[pos].copy(isUrgent = isUrgent)
            updatedTasks
        }
    }

    fun updateDoneState(isDone: Boolean, id: UUID, pos: Int) {
        taskRepository.updateDoneState(isDone, id)

        updateTasks { oldTasks ->
            val updatedTasks = oldTasks.toMutableList()
            updatedTasks[pos] = privateTasks.value[pos].copy(isDone = isDone)
            updatedTasks
        }
    }

    fun updateVisibility(index: Int, visibility: Boolean) {
        if (index < _detailsVisibility.size) _detailsVisibility[index] = visibility
    }
}