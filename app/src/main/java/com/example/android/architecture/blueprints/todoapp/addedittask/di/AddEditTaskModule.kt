package com.example.android.architecture.blueprints.todoapp.addedittask.di

import androidx.lifecycle.ViewModel
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskViewModel
import com.example.android.architecture.blueprints.todoapp.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class AddEditTaskModule {

    @Binds
    @IntoMap
    @ViewModelKey(AddEditTaskViewModel::class)
    abstract fun bindViewModel(viewmodel: AddEditTaskViewModel): ViewModel
}
