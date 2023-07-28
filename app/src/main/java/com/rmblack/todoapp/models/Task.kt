package com.rmblack.todoapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aminography.primecalendar.persian.PersianCalendar
import java.util.UUID

@Entity
data class Task(
    @PrimaryKey
    val id: UUID,
    var title: String,
    var description: String,
    val addedTime: PersianCalendar,
    val deadLine: PersianCalendar,
    var isUrgent: Boolean,
    var isDone: Boolean,
    val user: User,
    val groupId: String) {

    var detailsVisibility: Boolean = false

}
