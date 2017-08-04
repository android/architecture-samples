package com.example.android.architecture.blueprints.todoapp;

import android.app.Application;
import android.support.annotation.VisibleForTesting;

import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource;
import com.example.android.architecture.blueprints.todoapp.di.AppComponent;
import com.example.android.architecture.blueprints.todoapp.di.DaggerAppComponent;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;

/**
 * Even though Dagger2 allows annotating a {@link dagger.Component} as a singleton, the code itself
 * must ensure only one instance of the class is created. Therefore, we create a custom
 * {@link Application} class to store a singleton reference to the {@link
 * AppComponent}.
 * <p>
 * The application is made of 5 Dagger components, as follows:<BR />
 * {@link AppComponent}: the data (it encapsulates a db and server data)<BR />
 * completed<BR />
 */
public class ToDoApplication extends DaggerApplication {
    @Inject
    TasksDataSource tasksRepository;
    private AppComponent appcomponent;

    @Override
    public void onCreate() {
        super.onCreate();
        appcomponent.inject(this);
    }

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        appcomponent = DaggerAppComponent.builder().application(this).build();
        return appcomponent;
    }


    //Our Espresso tests need to be able to get an instance of the {@link TaskRespository}
    //so that we can delete all tasks before running each test
    @VisibleForTesting
    public TasksDataSource getTasksRepository() {
        return tasksRepository;
    }

}
