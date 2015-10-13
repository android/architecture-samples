/*
 * Copyright 2015, The Android Open Source Project
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

package com.example.android.testing.notes.util;

import android.support.test.espresso.IdlingResource;

/**
 * This is an empty EspressoIdlingResource used in non-test build types.
 * <p>
 * See `src/mock/java/com.example.android.testing.notes.util.EspressoIdlingResource`.
 */
public class EspressoIdlingResource {

    public static void increment() {
        // NOOP
    }

    public static void decrement() {
        // NOOP
    }

    public static IdlingResource getIdlingResource() {
        return null;
    }
}
