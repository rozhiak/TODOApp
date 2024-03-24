package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.ViewModel
import com.rmblack.todoapp.data.repository.TaskRepository
import javax.inject.Inject

class CalendarViewModel : ViewModel() {

    @Inject
    lateinit var taskRepository: TaskRepository

    val tasks = taskRepository.getTasksFlow()

}