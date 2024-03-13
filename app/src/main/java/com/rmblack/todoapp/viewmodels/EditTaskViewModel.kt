package com.rmblack.todoapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aminography.primecalendar.persian.PersianCalendar
import com.rmblack.todoapp.alarm.AlarmScheduler
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.models.server.success.User
import com.rmblack.todoapp.utils.SharedPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class EditTaskViewModel(taskId: UUID, private val alarmScheduler: AlarmScheduler, application: Application) :
    AndroidViewModel(application) {
    var doNotSave = false

    private val taskRepository = TaskRepository.get()

    private val sharedPreferencesManager = SharedPreferencesManager(application)

    private val _task: MutableStateFlow<Task?> = MutableStateFlow(null)
    val task: StateFlow<Task?> = _task.asStateFlow()

    lateinit var primaryTask: Task

    private var dataLoadedForFirstTime = false

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val task = taskRepository.getTaskFlow(taskId)
                task.collect {
                    if (!dataLoadedForFirstTime) {
                        dataLoadedForFirstTime = true
                        _task.value = it.copy()
                        primaryTask = it.copy()
                    } else {
                        /* If alarm triggered when user is on editing
                           alarm switch will be synced. */
                        _task.value = _task.value?.copy(
                            alarm = it.alarm
                        )
                    }
                }
            }
        }
    }

    fun setAlarm() {
        task.value?.let {
            val deadline = it.deadLine.timeInMillis
            val deadlineCopy = PersianCalendar()
            deadlineCopy.timeInMillis = deadline
            deadlineCopy.second = 0
            alarmScheduler.schedule(
                deadlineCopy.timeInMillis, it.id
            )
        }
    }

    fun cancelAlarm() {
        task.value?.let { task ->
            alarmScheduler.cancel(task.id)
        }
    }

    fun resetAlarmTime() {
        task.value?.let { task ->
            alarmScheduler.cancel(task.id)
            setAlarm()
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

    fun getUserFromSP(): User? {
        return sharedPreferencesManager.getUser()
    }

    fun getAutoStartPermissionState(): Boolean {
        return sharedPreferencesManager.getAutoStartPermissionCheckState()
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
    private val taskId: UUID, private val alarmScheduler: AlarmScheduler, private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return EditTaskViewModel(taskId, alarmScheduler, application) as T
    }
}