package com.rmblack.todoapp.applications

import android.app.Application
import com.aminography.primecalendar.persian.PersianCalendar
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.Task
import com.rmblack.todoapp.models.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.UUID

class TodoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TaskRepository.initialize(this)

        //
        val user1 = User("روژیاک محمدی", "09939139575", "1234", "5678")
        val task1 = Task(
            "نصب نرم افزار",
            UUID.randomUUID(),
            "باید روی کامپیوتر نصب شود.",
            PersianCalendar(),
            PersianCalendar(),
            isUrgent = false,
            isDone = false,
            isShared = false,
            user = user1,
            groupId = "1234"
        )

        val user2 = User("علی صالحی", "09939139576", "2345", "0987")
        val task2 = Task(
            "مشکل پنکه",
            UUID.randomUUID(),
            "سیم اتصالی دارد",
            PersianCalendar(),
            PersianCalendar(),
            isUrgent = false,
            isDone = false,
            isShared = false,
            user = user2,
            groupId = "1234"
        )

        val user3 = User("علی محمدی", "09939139577", "2345", "0987")
        val task3 = Task(
            "دیوار",
            UUID.randomUUID(),
            "دیوار باید خراب شود",
            PersianCalendar(),
            PersianCalendar(),
            isUrgent = false,
            isDone = true,
            isShared = true,
            user = user3,
            groupId = "1234"
        )
        val repo = TaskRepository.get()


//        GlobalScope.launch {
//            repo.insert(task1)
//            repo.insert(task2)
//            repo.insert(task3)
//        }


    }
}