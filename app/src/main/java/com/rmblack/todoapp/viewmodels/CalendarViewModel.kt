package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aminography.primecalendar.persian.PersianCalendar
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.local.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.mirrajabi.persiancalendar.core.models.PersianDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CalendarViewModel @Inject constructor(taskRepository: TaskRepository) : ViewModel() {

    val tasks = taskRepository.getTasksFlow()

    private var _selectedDate = MutableStateFlow<PersianDate?>(null)

    private val selectedDate = _selectedDate.asStateFlow()

    private var _events = MutableStateFlow<List<Task>?>(null)

    val events = _events.asStateFlow()

    init {
        viewModelScope.launch {
            tasks.collect {
                if (selectedDate.value != null) {

                }
            }

            selectedDate.collect { targetDate ->
                targetDate?.let {

                }
            }
        }
    }

    private fun PersianCalendar.areEqualInDate(year: Int, month: Int, day: Int): Boolean {
        if (this.year != year) {
            return false
        } else if(this.month != month) {
            return false
        } else if(this.dayOfMonth != day) {
            return false
        }
        return true
    }

    private fun setEvents(newValue: List<Task>) {
        _events.value = newValue
    }

    fun setSelectedDate(date: PersianDate) {
        _selectedDate.value = date
    }

}