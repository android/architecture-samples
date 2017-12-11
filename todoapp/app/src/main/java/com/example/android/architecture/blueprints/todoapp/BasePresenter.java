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

/**
 * We'll use generics here too for the view's type.
 */
public interface BasePresenter<IView extends BaseView> {
    void start();

    /**
     * Attaches a view to this presenter.
     * @param view {@link IView}
     */
    void attach(IView view);

    /**
     * Detaches the view from this presenter... this is the core solution
     * for the memory leak issue.
     *
     * We need to ensure we remove the reference of the view (Activity/Fragment/View)
     * from the presenter whenever the Android component's life ends.
     */
    void detach();
}
