package com.example.android.architecture.blueprints.todoapp.util;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

/**
 * Provides a method to show a Snackbar.
 */
public class SnackbarUtils {

    public static void showSnackbar(View v, String snackbarText) {
        if (v == null || snackbarText == null) {
            return;
        }
        Snackbar.make(v, snackbarText, Snackbar.LENGTH_LONG).show();
    }
}
