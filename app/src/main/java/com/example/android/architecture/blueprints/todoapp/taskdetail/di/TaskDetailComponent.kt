package com.example.android.architecture.blueprints.todoapp.taskdetail.di

import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailFragment
import dagger.Subcomponent

@Subcomponent(modules = [TaskDetailModule::class])
interface TaskDetailComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): TaskDetailComponent
    }

    fun inject(fragment: TaskDetailFragment)
}
