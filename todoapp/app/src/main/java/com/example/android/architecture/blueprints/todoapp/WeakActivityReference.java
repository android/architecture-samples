package com.example.android.architecture.blueprints.todoapp;

import java.lang.ref.WeakReference;

public class WeakActivityReference<T extends BaseActivity> {

    private WeakReference<T> mRef;

    public WeakActivityReference(T ref) {
        mRef = new WeakReference<>(ref);
    }

    /**
     * Returns null if the activity is destroyed or was GC'ed.
     */
    public T get() {
        if (mRef.get() == null || mRef.get().isDestroyed()) {
            return null;
        }
        return mRef.get();
    }
}
