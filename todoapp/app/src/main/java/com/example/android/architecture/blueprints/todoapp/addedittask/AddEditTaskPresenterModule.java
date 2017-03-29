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

    private boolean mShouldLoadDataFromRepo;

    public AddEditTaskPresenterModule(AddEditTaskContract.View view, @Nullable String taskId,
                                      boolean shouldLoadDataFromRepo) {
        mView = view;
        mTaskId = taskId;
        mShouldLoadDataFromRepo = shouldLoadDataFromRepo;
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

    @Provides
    boolean provideShouldLoadDataFromRepo() {
        return mShouldLoadDataFromRepo;
    }
}
