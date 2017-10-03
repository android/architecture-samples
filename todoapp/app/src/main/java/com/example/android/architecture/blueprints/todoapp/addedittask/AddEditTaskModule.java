package com.example.android.architecture.blueprints.todoapp.addedittask;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.Injection;
import com.example.android.architecture.blueprints.todoapp.util.providers.BaseNavigator;

/**
 * Enables inversion of control of the ViewModel and Navigator classes for adding and editing tasks.
 */
class AddEditTaskModule {

    @NonNull
    public static AddEditTaskViewModel createAddEditTaskViewModel(@Nullable String taskId,
                                                                  @NonNull Activity activity) {
        Context appContext = activity.getApplicationContext();
        BaseNavigator navigationProvider = Injection.createNavigationProvider(activity);
        return new AddEditTaskViewModel(taskId, Injection.provideTasksRepository(appContext),
                createAddEditTaskNavigator(navigationProvider));
    }

    @NonNull
    public static AddEditTaskNavigator createAddEditTaskNavigator(
            @NonNull BaseNavigator navigationProvider) {
        return new AddEditTaskNavigator(navigationProvider);
    }

}
