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
 * RxJava Subscription clone.
 * <p>
 * Subscription returns from {@link UseCaseHandler#execute(UseCase, UseCase.RequestValues, UseCase.Callback)} to allow unsubscribing.
 */
public interface Subscription {

    /**
     * Stops the receipt of notifications on the {@link UseCase.Callback} that was registered when this Subscription
     * was received.
     * <p>
     * This allows unregistering an {@link UseCase.Callback} before it has finished receiving all events (i.e. before
     * onCompleted is called).
     */
    void unsubscribe();

    /**
     * Indicates whether this {@code Subscription} is currently unsubscribed.
     *
     * @return {@code true} if this {@code Subscription} is currently unsubscribed, {@code false} otherwise
     */
    boolean isUnsubscribed();

}
