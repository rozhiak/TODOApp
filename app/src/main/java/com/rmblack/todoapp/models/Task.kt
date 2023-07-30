package com.rmblack.todoapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aminography.primecalendar.persian.PersianCalendar
import java.util.UUID

@Entity
data class Task(
    var title: String,
    @PrimaryKey
    val id: UUID,
    var description: String,
    val addedTime: PersianCalendar,
    val deadLine: PersianCalendar,
    var isUrgent: Boolean,
    var isDone: Boolean,
    var isShared: Boolean,
    val user: User,
    val groupId: String) {

    @Transient
    var detailsVisibility: Boolean = false

}
