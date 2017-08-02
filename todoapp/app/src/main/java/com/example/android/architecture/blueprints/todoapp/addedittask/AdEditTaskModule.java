package com.example.android.architecture.blueprints.todoapp.addedittask;

import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.util.PerActivity;
import com.example.android.architecture.blueprints.todoapp.util.PerFragment;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

/**
 * This is a Dagger module. We use this to auto create the AdEditTaskSubComponent and bind
 * the {@link AddEditTaskPresenter} to the graph
 */
@Module
public abstract class AdEditTaskModule {
    @PerFragment
    @ContributesAndroidInjector
    abstract AddEditTaskFragment addEditTaskFragment();

    @PerActivity
    @Binds
    abstract AddEditTaskContract.Presenter taskPresenter(AddEditTaskPresenter presenter);


    @Provides
    @PerActivity
    @Nullable
    static String provideTaskId(AddEditTaskActivity activity) {
        return activity.getIntent().getStringExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID);
    }
}
