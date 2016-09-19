package com.example.android.architecture.blueprints.todoapp.util.providers;

import android.support.annotation.Nullable;

/**
 * Provides navigation operations.
 */
public interface BaseNavigationProvider {

    void navigateToTaskDetails(@Nullable String taskId);
}
