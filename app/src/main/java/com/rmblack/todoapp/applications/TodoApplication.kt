package com.rmblack.todoapp.applications

import android.app.Application
import com.aminography.primecalendar.persian.PersianCalendar
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.Task
import com.rmblack.todoapp.models.TaskState
import com.rmblack.todoapp.models.User
import com.rmblack.todoapp.webservice.ApiService
import com.rmblack.todoapp.webservice.repository.ApiRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class TodoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TaskRepository.initialize(this)

        //
        val user1 = User("روژیاک محمدی", "09939139575", "1234", "5678")
        val deadline = PersianCalendar()
        deadline.set(1402, 4, 24)
        val task1 = Task(
            "نصب نرم افزار",
            UUID.randomUUID(),
            "باید روی کامپیوتر نصب شود.",
            PersianCalendar(),
            deadline,
            isUrgent = false,
            isDone = false,
            isShared = false,
            user = user1,
            groupId = "1234",
            TaskState.NEW

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
            groupId = "1234",
            TaskState.NEW

        )

        val user3 = User("علی محمدی", "09939139577", "2345", "0987")
        val task3 = Task(
            "دیوار",
            UUID.randomUUID(),
            "دیوار باید خراب شود",
            PersianCalendar(),
            PersianCalendar(),
            isUrgent = false,
            isDone = false,
            isShared = true,
            user = user3,
            groupId = "1234",
            TaskState.NEW

        )
        val repo = TaskRepository.get()


        GlobalScope.launch {
//            repo.addTask(task1)
//            repo.addTask(task2)
//            repo.addTask(task3)
        }

        newTaskTest()
    }

    //This is for testing
    //Response models should be implemented (for success and failure)
    fun newTaskTest() {
        val retrofitService = ApiService.getInstance()
        val repo = ApiRepository(retrofitService)

        CoroutineScope(Dispatchers.IO ).launch {
            val response = repo.addNewTask(
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJwaG9uZSI6IjA5MTIxMjM0NTY3In0.ieQ7vwW7LiwF2ZUboUYXxHAvLzoA1lhaLfEHczB_r-M",
                "عنوان",
                "12/12/12",
                "توضیح",
                "13/13/13",
                "false",
                "false",
                "false"
            )
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    println("RESPONSE  " + response.body())
                } else {
                    println("FAILURE  " + response.errorBody())
                }
            }
        }

    }
}