package com.example.android.architecture.blueprints.todoapp.di

import androidx.fragment.app.Fragment

interface InjectorProvider {
    val component: ApplicationComponent
}

val Fragment.injector get() = (requireActivity().applicationContext as InjectorProvider).component