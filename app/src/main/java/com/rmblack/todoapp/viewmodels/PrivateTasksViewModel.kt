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

class PrivateTasksViewModel(): TasksViewModel() {

    init {
        viewModelScope.launch {
            taskRepository.getPrivateTasks().collect {tasks ->
                _tasks.value = tasks.sortedBy { it.deadLine }

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

}