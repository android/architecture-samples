package com.example.android.architecture.blueprints.todoapp.tasks.di

import androidx.lifecycle.ViewModel
import com.example.android.architecture.blueprints.todoapp.di.ViewModelKey
import com.example.android.architecture.blueprints.todoapp.tasks.TasksViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class TasksModule {

    @Binds
    @IntoMap
    @ViewModelKey(TasksViewModel::class)
    abstract fun bindViewModel(viewmodel: TasksViewModel): ViewModel
}
