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

import android.databinding.Observable;
import android.support.design.widget.Snackbar;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Callback to apply to a {@link android.databinding.ObservableField<String>} that shows a Snackbar
 * whenever the text is updated.
 */
public class SnackBarChangedCallback extends Observable.OnPropertyChangedCallback {

    private final WeakReference<View> mView;

    private final SnackBarViewModel mViewModel;

    public SnackBarChangedCallback(View descendantOfCoordinatorLayout,
                                   SnackBarViewModel viewModel) {
        mView = new WeakReference<>(descendantOfCoordinatorLayout);
        mViewModel = viewModel;
    }

    @Override
    public void onPropertyChanged(Observable observable, int i) {
        Snackbar.make(mView.get(),
                mViewModel.getSnackBarText(),
                Snackbar.LENGTH_SHORT).show();
    }

    public interface SnackBarViewModel {
        String getSnackBarText();
    }
}
