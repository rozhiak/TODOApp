package com.rmblack.todoapp.models

import com.aminography.primecalendar.persian.PersianCalendar

data class Task(var title: String,
                val id: String,
                var description: String,
                val addedTime: PersianCalendar,
                val deadLine: PersianCalendar,
                var isUrgent: Boolean,
                var isDone: Boolean,
                val user: User,
                val groupId: String) {
                var detailsVisibility: Boolean = false
}
