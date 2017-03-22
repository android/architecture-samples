package com.example.android.architecture.blueprints.todoapp.util;

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by jalc on 3/22/17.
 */

public class SnackbarUtils {

    public static void showSnackBar(View v, String snackbarText) {
        if (v == null || snackbarText == null) {
            return;
        }
        Snackbar.make(v, snackbarText, Snackbar.LENGTH_LONG).show();
    }
}
