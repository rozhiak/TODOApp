package com.rmblack.todoapp.models.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aminography.primecalendar.persian.PersianCalendar
import java.util.UUID

@Entity
data class Task(
    var title: String,
    @PrimaryKey
    val id: UUID,
    val description: String,
    val addedTime: PersianCalendar,
    val deadLine: PersianCalendar,
    val isUrgent: Boolean,
    val isDone: Boolean,
    val isShared: Boolean,
    val user: User,
    val groupId: String,
    val serverID: String = ""
)
