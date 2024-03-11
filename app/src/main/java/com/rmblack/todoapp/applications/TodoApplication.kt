package com.rmblack.todoapp.applications

import android.app.Application
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.utils.Constants


class TodoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TaskRepository.initialize(this)
    }
}