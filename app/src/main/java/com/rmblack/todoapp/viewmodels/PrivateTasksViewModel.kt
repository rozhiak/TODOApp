package com.rmblack.todoapp.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.models.local.Task
import kotlinx.coroutines.launch

class PrivateTasksViewModel(application: Application) :
    TasksViewModel(application) {
    init {
        viewModelScope.launch {
            taskRepository.getPrivateTasksFlow().collect { tasks ->
                updateTasks(tasks)
            }
        }
    }

    private fun updateTasks(tasks: List<Task>) {
        val sortedTasks = tasks.sortedBy { it.deadLine }
        val tasksWithDatePositionNull = mutableListOf<Task?>()

        if (sortedTasks.isNotEmpty()) {
            tasksWithDatePositionNull.add(null)
            tasksWithDatePositionNull.add(sortedTasks[0]) //Because of if condition we can not access i - 1 position (-1)
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