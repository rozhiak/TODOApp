package com.rmblack.todoapp.applications

import android.app.Application
import com.aminography.primecalendar.persian.PersianCalendar
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.models.server.ServerTask
import com.rmblack.todoapp.models.local.Task
import com.rmblack.todoapp.models.local.TaskState
import com.rmblack.todoapp.models.local.User
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
    }
}