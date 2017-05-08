package com.example.android.architecture.blueprints.todoapp.taskdetail;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.Injection;
import com.example.android.architecture.blueprints.todoapp.util.providers.BaseNavigationProvider;

/**
 * Enables inversion of control of the ViewModel and Navigator classes for task detail.
 */
class TaskDetailModule {

    @NonNull
    public static TaskDetailViewModel createTaskDetailsViewModel(
            @Nullable String taskId,
            @NonNull Activity activity) {
        Context appContext = activity.getApplicationContext();
        BaseNavigationProvider navigationProvider = Injection.createNavigationProvider(activity);
        return new TaskDetailViewModel(taskId, Injection.provideTasksRepository(appContext),
                createTaskDetailNavigator(navigationProvider));
    }

    @NonNull
    public static TaskDetailNavigator createTaskDetailNavigator(
            @NonNull BaseNavigationProvider navigationProvider) {
        return new TaskDetailNavigator(navigationProvider);
    }

}
