package com.example.android.architecture.blueprints.todoapp.util.providers;

import android.app.Activity;

import java.lang.ref.WeakReference;

/**
 * Implementation of the {@link BaseNavigationProvider}.
 */

public class NavigationProvider implements BaseNavigationProvider {

    private final WeakReference<Activity> mActivity;

    public NavigationProvider(Activity activity) {
        mActivity = new WeakReference<>(activity);
    }

    @Override
    public void finishActivityWithResult(int resultCode) {
        mActivity.get().setResult(resultCode);
        mActivity.get().finish();
    }
}
