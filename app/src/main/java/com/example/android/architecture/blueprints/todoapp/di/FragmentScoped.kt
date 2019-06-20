package com.example.android.architecture.blueprints.todoapp.di


import javax.inject.Scope
import kotlin.annotation.AnnotationRetention.RUNTIME

@Scope
@Retention(RUNTIME)
@Target(
    AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER
)
annotation class FragmentScoped