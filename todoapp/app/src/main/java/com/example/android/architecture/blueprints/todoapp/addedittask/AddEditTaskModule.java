package com.example.android.architecture.blueprints.todoapp.addedittask;

import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.di.PerActivity;
import com.example.android.architecture.blueprints.todoapp.di.PerFragment;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

/**
 * This is a Dagger module. We use this to auto create the AdEditTaskSubComponent and bind
 * the {@link AddEditTaskPresenter} to the graph
 */
@Module
public abstract class AddEditTaskModule {
    //Rather than having the activity deal with getting the intent extra and passing it to the presenter
    //we will provide the taskId directly into the AddEditTaskActivitySubcomponent
    // which is what gets generated for us by Dagger.Android.
    // We can then inject our TaskId into our Presenter without having pass through dependency from the Activity.
    // Each UI object gets the dependency it needs and nothing else.
    @Provides
    @PerActivity
    @Nullable
    static String provideTaskId(AddEditTaskActivity activity) {
        return activity.getIntent().getStringExtra(AddEditTaskFragment.ARGUMENT_EDIT_TASK_ID);
    }

    @PerFragment
    @ContributesAndroidInjector
    abstract AddEditTaskFragment addEditTaskFragment();

    @PerActivity
    @Binds
    abstract AddEditTaskContract.Presenter taskPresenter(AddEditTaskPresenter presenter);
}
