package com.example.android.architecture.blueprints.todoapp.addedittask;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.example.android.architecture.blueprints.todoapp.util.providers.BaseNavigationProvider;

/**
 * Defines the navigation actions that can be called from AddEditTask screen
 */
public class AddEditTaskNavigator {

    @NonNull
    private final BaseNavigationProvider mNavigationProvider;

    public AddEditTaskNavigator(@NonNull BaseNavigationProvider navigationProvider) {
        mNavigationProvider = navigationProvider;
    }

    /**
     * When the task was saved, the activity should finish with success.
     */
    public void onTaskSaved() {
        mNavigationProvider.finishActivityWithResult(Activity.RESULT_OK);
    }
}
