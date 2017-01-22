package com.example.android.architecture.blueprints.todoapp.util.providers;

/**
 * TODO
 */

public interface BaseNavigationProvider {

    /**
     * Finish an Activity with a result.
     *
     * @param resultCode the result code to be set when finishing the Activity.
     */
    void finishActivityWithResult(int resultCode);
}
