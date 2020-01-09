package com.example.android.architecture.blueprints.todoapp.addedittask.di

import com.example.android.architecture.blueprints.todoapp.addedittask.AddEditTaskFragment
import dagger.Subcomponent

@Subcomponent(modules = [AddEditTaskModule::class])
interface AddEditTaskComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): AddEditTaskComponent
    }

    fun inject(fragment: AddEditTaskFragment)
}
