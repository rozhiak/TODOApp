package com.rmblack.todoapp.viewmodels

import androidx.lifecycle.ViewModel
import com.aminography.primecalendar.persian.PersianCalendar
import com.rmblack.todoapp.models.Task
import com.rmblack.todoapp.models.User
import java.util.UUID

class SharedTasksViewModel: ViewModel() {

    val tasks = mutableListOf<Task>()

    init {
        for (i in 0 until 100) {
            val task = Task(
                title = "تسک شماره #$i",
                id = UUID.randomUUID(),
                deadLine = PersianCalendar(),
                description = "توضحات برای #$i",
                addedTime = PersianCalendar(),
                isUrgent = i % 9 == 0,
                user = User("محمد", "09939139575", "1234", "1324"),
                isDone = false,
                groupId = "1234"
            )
            tasks += task
        }
    }

}