package com.rmblack.todoapp.di

import com.rmblack.todoapp.data.repository.TaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class TaskRepoModule {
    @Singleton
    @Provides
    fun provideTaskRepository(): TaskRepository {
        return TaskRepository.get()
    }
}