package com.example.android.architecture.blueprints.todoapp.tasks.di

import com.example.android.architecture.blueprints.todoapp.tasks.TasksFragment
import dagger.Subcomponent

@Subcomponent(modules = [TasksModule::class])
interface TasksComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): TasksComponent
    }

    fun inject(fragment: TasksFragment)
}
