package com.example.android.architecture.blueprints.todoapp.util.providers;

import android.app.Activity;
import android.content.Intent;

import java.lang.ref.WeakReference;

/**
 * Implementation of the {@link BaseNavigationProvider}.
 */
public final class NavigationProvider implements BaseNavigationProvider {

    private final WeakReference<Activity> mActivity;

    public NavigationProvider(Activity activity) {
        mActivity = new WeakReference<>(activity);
    }

    @Override
    public void finishActivity() {
        if (mActivity.get() != null) {
            mActivity.get().finish();
        }
    }

    @Override
    public void finishActivityWithResult(int resultCode) {
        if (mActivity.get() != null) {
            mActivity.get().setResult(resultCode);
            mActivity.get().finish();
        }
    }

    @Override
    public void startActivityForResult(Class cls, int requestCode) {
        if (mActivity.get() != null) {
            Intent intent = new Intent(mActivity.get(), cls);
            mActivity.get().startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public void startActivityForResultWithExtra(Class cls, int requestCode, String extraKey,
                                                String extraValue) {
        if (mActivity.get() != null) {
            Intent intent = new Intent(mActivity.get(), cls);
            intent.putExtra(extraKey, extraValue);
            mActivity.get().startActivityForResult(intent, requestCode);
        }
    }
}
