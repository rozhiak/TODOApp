package com.rmblack.todoapp.applications

import android.app.Application
import com.rmblack.todoapp.data.repository.TaskRepository

class TodoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TaskRepository.initialize(this)
    }
}