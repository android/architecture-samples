package com.example.android.architecture.blueprints.todoapp.util.providers;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

/**
 * Resolves application's resources.
 */
public interface BaseResourceProvider {

    /**
     * Resolves text's id to String.
     *
     * @param id to be fetched from the resources
     * @return String representation of the {@param id}
     */
    @NonNull
    String getString(@StringRes final int id);

    /**
     * Resolves text's id to String and formats it.
     *
     * @param resId      to be fetched from the resources
     * @param formatArgs format arguments
     * @return String representation of the {@param resId}
     */
    @NonNull
    String getString(@StringRes final int resId, final Object... formatArgs);
}
