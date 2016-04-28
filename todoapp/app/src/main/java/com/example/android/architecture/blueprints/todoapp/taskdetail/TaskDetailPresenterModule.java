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

    private final String mTaskId;

    public TaskDetailPresenterModule(TaskDetailContract.View view, String taskId) {
        mView = view;
        mTaskId = taskId;
    }

    @Provides
    TaskDetailContract.View provideTaskDetailContractView() {
        return mView;
    }

    @Provides
    String provideTaskId() {
        return mTaskId;
    }
}
