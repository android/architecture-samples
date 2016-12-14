/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;


/**
 * TODO: javadoc
 */
public class SnackBarProxy {

    private static final String TAG = "BoundSnackBar";

    private final WeakReference<View> mSnackBarView;

    private final Context mContext;

    // Prevent direct instantiation.
    private SnackBarProxy(Context context, View snackBarView){
        mContext = context;
        mSnackBarView = new WeakReference<>(snackBarView);
    }

    public static SnackBarProxy getInstance(Activity activity, @IdRes int id) {
        return new SnackBarProxy(activity.getApplicationContext(), activity.findViewById(id));
    }

    public void showMessage(@StringRes int string) {
        showMessage(mContext.getString(string));
    }

    private void showMessage(String msg) {
        if (mSnackBarView == null) {
            Log.e(TAG, "Snackbar was not bound");
            return;
        }
        if (mSnackBarView.get() == null) {
            Log.e(TAG, "ERROR: Snackbar view is null");
            return;
        }

        Snackbar.make(mSnackBarView.get(), msg, Snackbar.LENGTH_SHORT).show();
    }
}
