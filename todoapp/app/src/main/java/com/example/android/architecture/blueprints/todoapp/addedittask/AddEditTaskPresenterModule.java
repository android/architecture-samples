package com.example.android.architecture.blueprints.todoapp.addedittask;

import android.support.annotation.Nullable;

import dagger.Module;
import dagger.Provides;

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * {@link AddEditTaskPresenter}.
 */
@Module
public class AddEditTaskPresenterModule {

    private final AddEditTaskContract.View mView;

    private String mTaskId;

    public AddEditTaskPresenterModule(AddEditTaskContract.View view, @Nullable String taskId) {
        mView = view;
        mTaskId = taskId;
    }

    @Provides
    AddEditTaskContract.View provideAddEditTaskContractView() {
        return mView;
    }

    @Provides
    @Nullable
    String provideTaskId() {
        return mTaskId;
    }
}
