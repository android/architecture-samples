package com.example.android.architecture.blueprints.todoapp.util.providers;

/**
 * Handles navigation between Activities in the app.
 */

public interface BaseNavigationProvider {

    /**
     * Finish an Activity with a result.
     *
     * @param resultCode the result code to be set when finishing the Activity.
     */
    void finishActivityWithResult(int resultCode);

    /**
     * Start a new Activity for a result.
     *
     * @param cls         the Activity class to be opened.
     * @param requestCode the request code that will be passed to the opened Activity.
     */
    void startActivityForResult(Class cls, int requestCode);
}
