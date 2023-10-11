package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.utils.SharedPreferencesManager
import kotlinx.coroutines.launch
class SharedTasksViewModel(sharedPreferencesManager: SharedPreferencesManager): TasksViewModel(sharedPreferencesManager) {

    init {
        viewModelScope.launch {
            taskRepository.getSharedTasksFlow().collect { tasks ->
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

                _tasks.value = tasksWithDatePositionNull.toList()
            }
        }
    }
}