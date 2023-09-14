package com.rmblack.todoapp.data.database

import androidx.room.TypeConverter
import com.aminography.primecalendar.persian.PersianCalendar
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
}