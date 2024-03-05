package com.rmblack.todoapp.applications

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import com.rmblack.todoapp.R
import com.rmblack.todoapp.data.repository.TaskRepository
import com.rmblack.todoapp.utils.Constants


class TodoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        TaskRepository.initialize(this)
    }
}