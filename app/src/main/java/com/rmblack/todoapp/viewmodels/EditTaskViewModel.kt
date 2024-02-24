package com.rmblack.todoapp.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.utils.AlarmUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class EditTaskViewModel(taskId: UUID) : ViewModel() {
    var doNotSave = false

    private val taskRepository = TaskRepository.get()

    private val _task: MutableStateFlow<Task?> = MutableStateFlow(null)
    val task: StateFlow<Task?> = _task.asStateFlow()

    lateinit var primaryTask: Task

    init {
        viewModelScope.launch {
            val task = withContext(Dispatchers.IO) {
                taskRepository.getTask(taskId)
            }
            primaryTask = task.copy()
            _task.value = task
        }
    }

    fun setAlarm(context: Context) {
        task.value?.let {
            val alarmResult = AlarmUtil.setAlarm(
                context, it.deadLine.timeInMillis, it.id, AlarmUtil.ALARM_DEADLINE
            )

            updateTask { oldTask ->
                oldTask.copy(
                    alarm = alarmResult
                )
            }
        }
    }

    fun cancelAlarm(context: Context) {
        task.value?.let { task ->
            AlarmUtil.cancelAlarm(context, task.id, AlarmUtil.ALARM_DEADLINE)
        }

        updateTask { oldTask ->
            oldTask.copy(
                alarm = false
            )
        }
    }

    fun saveTitleAndDescription(newTitle: String, newDes: String) {
        val newTitleTrimmed = newTitle.trimEnd()
        val newDescriptionTrimmed = newDes.trimEnd()

        if (newTitleTrimmed != task.value?.title || newDescriptionTrimmed != task.value?.description) {
            updateTask { oldTask ->
                oldTask.copy(
                    title = newTitleTrimmed,
                    description = newDescriptionTrimmed,
                )
            }
        }
    }

    fun updateTask(onUpdate: (Task) -> Task) {
        _task.update { oldTask ->
            oldTask?.let {
                onUpdate(it)
            }
        }
    }

    fun deleteTaskFromRoom(task: Task) {
        taskRepository.deleteTask(task)
    }

    fun updateTaskInRoom(task: Task) {
        taskRepository.updateTask(task)
    }

    override fun onCleared() {
        super.onCleared()
        if (!doNotSave) {
            task.value?.let {
                if ((it.title.isEmpty() || it.title.isBlank()) && primaryTask.title.isNotBlank()) {
                    //Do nothing
                } else if (it.title.isEmpty() || it.title.isBlank()) {
                    deleteTaskFromRoom(it)
                } else {
                    updateTaskInRoom(it)
                }
            }
        }
    }
}

class EditTaskViewModelFactory(
    private val taskId: UUID
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditTaskViewModel(taskId) as T
    }
}