package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.webservice.repository.ApiRepository
import kotlinx.coroutines.launch
class SharedTasksViewModel: TasksViewModel() {

    init {
        viewModelScope.launch {
            taskRepository.getSharedTasks().collect {tasks ->
                val sortedTasks = tasks.sortedBy { it.deadLine }
                val tasksWithDatePositionNull = mutableListOf<Task?>()

                if (sortedTasks.isNotEmpty()) {
                    tasksWithDatePositionNull.add(null)
                    tasksWithDatePositionNull.add(sortedTasks[0])
                    for (i in 1 until sortedTasks.size) {
                        if (sortedTasks[i].deadLine.shortDateString != sortedTasks[i - 1].deadLine.shortDateString) {
                            tasksWithDatePositionNull.add(null)
                        }
                        tasksWithDatePositionNull.add(sortedTasks[i])
                    }
                }

                while (_detailsVisibility.size < tasksWithDatePositionNull.size) {
                    _detailsVisibility.add(false)
                }

                _tasks.value = tasksWithDatePositionNull.toList()
            }
        }
    }
}