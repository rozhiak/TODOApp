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

    private var _events = MutableStateFlow<List<Task>?>(null)

    val events = _events.asStateFlow()

    private var _selectedDay: PersianDate? = null

    val selectedDay = _selectedDay

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

    fun setEvents(date: PersianDate) {
        viewModelScope.launch {
            tasks.collect {
                _events.value = it.filter { task ->
                    task.deadLine.areEqualInDate(
                        date.year,
                        date.month - 1, // Persian calendar months start from 0
                        date.dayOfMonth
                    )
                }
            }
        }
    }

    fun setSelectedDay(date: PersianDate) {
        _selectedDay = date
    }

}