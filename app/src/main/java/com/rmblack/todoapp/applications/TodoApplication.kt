package com.rmblack.todoapp.applications

import android.app.Application
import com.rmblack.todoapp.data.repository.TaskRepository
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TodoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TaskRepository.initialize(this)
    }
}