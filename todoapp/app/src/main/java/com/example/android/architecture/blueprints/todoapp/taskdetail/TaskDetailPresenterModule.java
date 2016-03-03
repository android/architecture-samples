package com.example.android.architecture.blueprints.todoapp.taskdetail;

import dagger.Module;
import dagger.Provides;

/**
 * This is a Dagger module. We use this to pass in the View dependency to the
 * {@link TaskDetailPresenter}.
 */
@Module
public class TaskDetailPresenterModule {

    private final TaskDetailContract.View mView;

    public TaskDetailPresenterModule(TaskDetailContract.View view) {
        mView = view;
    }

    @Provides
    TaskDetailContract.View provideTaskDetailContractView() {
        return mView;
    }
}
