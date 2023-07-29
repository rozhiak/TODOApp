package com.rmblack.todoapp.database

import androidx.room.TypeConverter
import com.aminography.primecalendar.persian.PersianCalendar
import com.rmblack.todoapp.models.User
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TaskTypeConverters {
    @TypeConverter
    fun fromPersianCalendar(persianCalendar: PersianCalendar): Long {
        return persianCalendar.timeInMillis
    }

    @TypeConverter
    fun toPersianCalendar(millis: Long): PersianCalendar {
        val persianCalendar = PersianCalendar()
        persianCalendar.timeInMillis = millis
        return persianCalendar
    }

    @TypeConverter
    fun fromUser(user: User): String {
        return Json.encodeToString(user)
    }

    @TypeConverter
    fun toUser(encoded: String): User {
        return Json.decodeFromString(encoded)
    }
}