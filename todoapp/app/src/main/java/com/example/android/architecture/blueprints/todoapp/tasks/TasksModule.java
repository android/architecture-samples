package com.example.android.architecture.blueprints.todoapp.tasks;

import com.example.android.architecture.blueprints.todoapp.di.PerActivity;
import com.example.android.architecture.blueprints.todoapp.di.PerFragment;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * {@link TasksPresenter}.
 */
@Module
public abstract class TasksModule {
    @PerFragment
    @ContributesAndroidInjector
    abstract TasksFragment tasksFragment();

    @PerActivity
    @Binds abstract TasksContract.Presenter taskPresenter(TasksPresenter presenter);
}
