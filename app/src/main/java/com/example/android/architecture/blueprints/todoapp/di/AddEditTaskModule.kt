package com.example.android.architecture.blueprints.todoapp.di

import androidx.lifecycle.ViewModel
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment
import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

/**
 * Dagger module for the Add/Edit feature.
 */
@Module
abstract class AddEditTaskModule {

    @FragmentScoped
    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun addEditTaskFragment(): AddEditTaskFragment

    @FragmentScoped
    @Binds
    @IntoMap
    @ViewModelKey(AddEditTaskViewModel::class)
    internal abstract fun bindViewModel(viewmodel: AddEditTaskViewModel): ViewModel
}
