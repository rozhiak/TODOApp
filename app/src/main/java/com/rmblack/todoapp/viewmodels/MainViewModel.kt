package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.ViewModel
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.local.Task

class MainViewModel: ViewModel() {

    private val taskRepository = TaskRepository.get()

    suspend fun addTask(task: Task) {
        taskRepository.addTask(task)
    }

}