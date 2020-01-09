package com.example.android.architecture.blueprints.todoapp.taskdetail.di

import androidx.lifecycle.ViewModel
import com.example.android.architecture.blueprints.todoapp.di.ViewModelKey
import com.example.android.architecture.blueprints.todoapp.taskdetail.TaskDetailViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class TaskDetailModule {

    @Binds
    @IntoMap
    @ViewModelKey(TaskDetailViewModel::class)
    abstract fun bindViewModel(viewmodel: TaskDetailViewModel): ViewModel
}
