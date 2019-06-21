package com.example.android.architecture.blueprints.todoapp.di

import android.content.Context
import com.example.android.architecture.blueprints.todoapp.TodoApplication
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        ApplicationModule::class,
        AndroidSupportInjectionModule::class,
        TasksModule::class,
        TaskDetailModule::class,
        AddEditTaskModule::class,
        StatisticsModule::class
    ])
interface ApplicationComponent : AndroidInjector<TodoApplication> {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance applicationContext: Context): ApplicationComponent
    }

    val tasksRepository: TasksRepository
}

