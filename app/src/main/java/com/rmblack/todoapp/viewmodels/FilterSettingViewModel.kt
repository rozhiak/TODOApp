package com.rmblack.todoapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.rmblack.todoapp.utils.SharedPreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FilterSettingViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferencesManager = SharedPreferencesManager(application)

    private val _doNotShowDoneTasksState =
        MutableStateFlow(sharedPreferencesManager.getDoNotShowDoneTasksState())

    val doNotShowDoneTasksState: StateFlow<Boolean>
        get() = _doNotShowDoneTasksState

    fun setDoNotShowDoneTasks(state: Boolean) {
        _doNotShowDoneTasksState.value = state
    }

    fun saveDoneTasksFilterState() {
        if (doNotShowDoneTasksState.value != sharedPreferencesManager.getDoNotShowDoneTasksState()) {
            sharedPreferencesManager.setDoNotShowDoneTasks(doNotShowDoneTasksState.value)
        }
    }

}