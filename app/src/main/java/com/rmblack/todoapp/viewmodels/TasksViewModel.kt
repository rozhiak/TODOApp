package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.ViewModel
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

open class TasksViewModel: ViewModel() {


    val taskRepository = TaskRepository.get()

    protected val _tasks: MutableStateFlow<List<Task>> = MutableStateFlow(emptyList())

    val tasks: StateFlow<List<Task>>
        get() = _tasks.asStateFlow()

    protected val _detailsVisibility: ArrayList<Boolean> = ArrayList()

    val detailsVisibility: List<Boolean>
        get() = _detailsVisibility.toList()

    private fun updateTasks(onUpdate: (List<Task>) -> List<Task>) {
        _tasks.update { oldTasks ->
            onUpdate(oldTasks)
        }
    }

    fun updateUrgentState(isUrgent: Boolean, id: UUID, pos: Int) {
        taskRepository.updateUrgentState(isUrgent, id)

        updateTasks { oldTasks ->
            val updatedTasks = oldTasks.toMutableList()
            updatedTasks[pos] = tasks.value[pos].copy(isUrgent = isUrgent)
            updatedTasks
        }
    }

    fun updateDoneState(isDone: Boolean, id: UUID, pos: Int) {
        taskRepository.updateDoneState(isDone, id)

        updateTasks { oldTasks ->
            val updatedTasks = oldTasks.toMutableList()
            updatedTasks[pos] = tasks.value[pos].copy(isDone = isDone)
            updatedTasks
        }
    }

    fun updateVisibility(index: Int, visibility: Boolean) {
        if (index < _detailsVisibility.size) _detailsVisibility[index] = visibility
    }

}