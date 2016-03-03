package com.example.android.architecture.blueprints.todoapp.tasks;

import dagger.Module;
import dagger.Provides;

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * {@link TasksPresenter}.
 */
@Module
public class TasksPresenterModule {

    private final TasksContract.View mView;

    public TasksPresenterModule(TasksContract.View view) {
        mView = view;
    }

    @Provides
    TasksContract.View provideTasksContractView() {
        return mView;
    }

}
