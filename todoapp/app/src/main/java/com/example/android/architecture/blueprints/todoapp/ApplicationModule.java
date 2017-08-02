package com.example.android.architecture.blueprints.todoapp;

import android.app.Application;
import android.content.Context;

import com.example.android.architecture.blueprints.todoapp.di.AppComponent;

import dagger.Binds;
import dagger.Module;

/**
 * This is a Dagger module. We use this to bind our Application class as a Context in the TaskRepositoryComponent
 * {@link
 * AppComponent}.
 */
@Module
public abstract class ApplicationModule {
    @Binds
    abstract Context bindContext(Application application);
}