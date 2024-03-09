package com.rmblack.todoapp.viewmodels

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.viewModelScope
import com.rmblack.todoapp.alarm.AlarmScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SharedTasksViewModel(application: Application, alarmScheduler: AlarmScheduler) :
    TasksViewModel(application, alarmScheduler),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val sharedTasksFlow = taskRepository.getSharedTasksFlow()

    init {
        viewModelScope.launch {
            sharedTasksFlow.collect { tasks ->
                updateTasks(tasks)
            }
        }

        sharedPreferencesManager.registerChangeListener(this)
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        if (p1 == "DO_NOT_SHOW_DONE_TASKS_KEY") {
            changeDoNotShowDoneTasksState()

            viewModelScope.launch {
                updateTasks(sharedTasksFlow.first())
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sharedPreferencesManager.unregisterChangeListener(this)
    }
}