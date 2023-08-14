package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.collections.ArrayList

class PrivateTasksViewModel(): ViewModel() {

    private val taskRepository = TaskRepository.get()

    private val _privateTasks: MutableStateFlow<List<Task>> = MutableStateFlow(emptyList())

    val privateTasks: StateFlow<List<Task>>
        get() = _privateTasks.asStateFlow()

    private val _detailsVisibility: ArrayList<Boolean> = ArrayList()

    val detailsVisibility: List<Boolean>
        get() = _detailsVisibility.toList()

    init {
        viewModelScope.launch {
            taskRepository.getPrivateTasks().collect {tasks ->
                _privateTasks.value = tasks.sortedBy { it.deadLine }

                while (_detailsVisibility.size < tasks.size) {
                    _detailsVisibility.add(false)
                }
                if(_detailsVisibility.size > tasks.size) {
                    _detailsVisibility.removeAt(0)
                    for (i in _detailsVisibility.indices) {
                        if (detailsVisibility[i]) {
                            _detailsVisibility[i] = false
                            break
                        }
                    }
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