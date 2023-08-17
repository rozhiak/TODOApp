package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
class SharedTasksViewModel: TasksViewModel() {

    init {
        viewModelScope.launch {
            taskRepository.getSharedTasks().collect {tasks ->
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