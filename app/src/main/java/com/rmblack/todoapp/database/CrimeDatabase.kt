package com.rmblack.todoapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rmblack.todoapp.models.Task

@Database(entities = [Task::class], version = 1)
abstract class CrimeDatabase: RoomDatabase() {

}