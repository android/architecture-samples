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


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Runs {@link UseCase}s using a {@link UseCaseScheduler}.
 */
public class UseCaseHandler {

    private static final UseCaseHandler INSTANCE = new UseCaseHandler(new UseCaseThreadPoolScheduler(), new UseCaseUiScheduler());
    private UseCaseScheduler executeScheduler;
    private UseCaseScheduler callbackScheduler;

    public static UseCaseHandler getInstance() {
        return INSTANCE;
    }

    public UseCaseHandler(UseCaseScheduler executeScheduler, UseCaseScheduler callbackScheduler) {
        this.executeScheduler = executeScheduler;
        this.callbackScheduler = callbackScheduler;
    }

    public <T extends UseCase.RequestValues, R extends UseCase.ResponseValue> Subscription execute(
            @NonNull final UseCase<T, R> useCase, @NonNull final T request, @Nullable UseCase.Callback<R> callback) {
        if (callback == null) {
            callback = new VoidCallback();
        } else {
            callback = new CallbackSchedulerWrapper(callback, callbackScheduler);
        }

        final CallbackSubscriptionWrapper<R> callbackWrapper = new CallbackSubscriptionWrapper<>(callback);

        // The network request might be handled in a different thread so make sure
        // Espresso knows
        // that the app is busy until the response is handled.
        EspressoIdlingResource.increment(); // App is busy until further notice

        executeScheduler.execute(new Runnable() {
            @Override
            public void run() {
                useCase.executeUseCase(request, callbackWrapper, callbackWrapper);

                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                    EspressoIdlingResource.decrement(); // Set app as idle.
                }
            }
        });

        return callbackWrapper;
    }

    /**
     * Redirects/Executes callback events on given {@code UseCaseScheduler}
     *
     * @param <V> the callback response type
     */
    private static final class CallbackSchedulerWrapper<V extends UseCase.ResponseValue> implements
            UseCase.Callback<V> {
        private final UseCase.Callback<V> callback;
        private final UseCaseScheduler scheduler;

        public CallbackSchedulerWrapper(UseCase.Callback<V> callback, UseCaseScheduler scheduler) {
            this.callback = callback;
            this.scheduler = scheduler;
        }

        @Override
        public void onStart() {
            scheduler.execute(new Runnable() {
                @Override
                public void run() {
                    callback.onStart();
                }
            });
        }

        @Override
        public void onNext(final V response) {
            scheduler.execute(new Runnable() {
                @Override
                public void run() {
                    callback.onNext(response);
                }
            });
        }

        @Override
        public void onCompleted() {
            scheduler.execute(new Runnable() {
                @Override
                public void run() {
                    callback.onCompleted();
                }
            });
        }

        @Override
        public void onError(final Throwable exception) {
            scheduler.execute(new Runnable() {
                @Override
                public void run() {
                    callback.onError(exception);
                }
            });
        }
    }

    /**
     * Stops the receipt of notifications on the wrapped {@link UseCase.Callback}
     *
     * @param <V> the callback response type
     */
    private static final class CallbackSubscriptionWrapper<V extends UseCase.ResponseValue> implements UseCase.Callback<V>, Subscription {
        private UseCase.Callback<V> callback;
        private AtomicBoolean unsubscribed = new AtomicBoolean(false);

        public CallbackSubscriptionWrapper(UseCase.Callback<V> callback) {
            this.callback = callback;
        }

        @Override
        public void onStart() {
            if (isUnsubscribed()) {
                return;
            }

            UseCase.Callback<V> callback = this.callback;

            if (callback != null) {
                callback.onStart();
            }
        }

        @Override
        public void onNext(V response) {
            if (isUnsubscribed()) {
                return;
            }

            UseCase.Callback<V> callback = this.callback;

            if (callback != null) {
                callback.onNext(response);
            }
        }

        @Override
        public void onCompleted() {
            if (isUnsubscribed()) {
                return;
            }

            UseCase.Callback<V> callback = this.callback;

            if (callback != null) {
                callback.onCompleted();
            }

            unsubscribe();
        }

        @Override
        public void onError(Throwable exception) {
            if (isUnsubscribed()) {
                return;
            }

            UseCase.Callback<V> callback = this.callback;

            if (callback != null) {
                callback.onError(exception);
            }

            unsubscribe();
        }

        @Override
        public void unsubscribe() {
            unsubscribed.set(true);
            callback = null;
        }

        @Override
        public boolean isUnsubscribed() {
            return unsubscribed.get();
        }
    }

    /**
     * Consumes callback events without taking any action
     */
    private static class VoidCallback implements UseCase.Callback {
        @Override
        public void onStart() {
            // Take no-action
        }

        @Override
        public void onNext(Object responseValues) {
            // Take no-action
        }

        @Override
        public void onCompleted() {
            // Take no-action
        }

        @Override
        public void onError(Throwable exception) {
            // Take no-action
        }
    }
}
