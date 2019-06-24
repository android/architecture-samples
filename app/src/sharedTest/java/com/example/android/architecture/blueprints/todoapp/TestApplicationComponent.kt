package com.example.android.architecture.blueprints.todoapp

import android.content.Context
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.di.AddEditTaskModule
import com.example.android.architecture.blueprints.todoapp.di.StatisticsModule
import com.example.android.architecture.blueprints.todoapp.di.TaskDetailModule
import com.example.android.architecture.blueprints.todoapp.di.TasksModule
import com.example.android.architecture.blueprints.todoapp.di.ViewModelBuilder
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
    TestApplicationModule::class,
    AndroidSupportInjectionModule::class,
    ViewModelBuilder::class,
    TasksModule::class,
    TaskDetailModule::class,
    AddEditTaskModule::class,
    StatisticsModule::class])
interface TestApplicationComponent : AndroidInjector<TestTodoApplication> {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance applicationContext: Context): TestApplicationComponent
    }


    val tasksRepository: TasksRepository
}
