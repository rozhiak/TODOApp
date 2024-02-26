package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.local.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class AlarmViewModel: ViewModel() {

    private val taskRepository = TaskRepository.get()

    private var _task = MutableLiveData<Task>()
    val task: LiveData<Task>
        get() = _task

    fun setData(id: UUID) {
        viewModelScope.launch {
            val task = withContext(Dispatchers.IO) {
                taskRepository.getTask(id)
            }
            _task.value = task
        }
    }

    fun resetAlarmState() {
        task.value?.let {
            taskRepository.updateAlarm(it.id, false)
        }
    }

}