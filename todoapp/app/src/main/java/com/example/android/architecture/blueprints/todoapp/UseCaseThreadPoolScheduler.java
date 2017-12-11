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

import android.os.Handler;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Executes asynchronous tasks using a {@link ThreadPoolExecutor}.
 * <p>
 * See also {@link Executors} for a list of factory methods to create common
 * {@link java.util.concurrent.ExecutorService}s for different scenarios.
 */
public class UseCaseThreadPoolScheduler implements UseCaseScheduler {
    /* Constants for thread-pool */
    private static final int POOL_SIZE     = 2;
    private static final int MAX_POOL_SIZE = 4;
    private static final int TIMEOUT      = 30;

    /* Stores reference to handler for UI-Thread */
    private final Handler mHandler = new Handler();

    /* Stores reference to thread-pool executor */
    private static ThreadPoolExecutor mThreadPoolExecutor;


    @Override
    public void execute(Runnable runnable) {
        getExecutor().execute(runnable);
    }

    @Override
    public void shutdownExecution() {
        if (mThreadPoolExecutor != null) {
            mThreadPoolExecutor.shutdownNow();
            mThreadPoolExecutor = null;
        }
    }

    @Override
    public <V extends UseCase.ResponseValue> void notifyResponse(final V response,
            final UseCase.UseCaseCallback<V> useCaseCallback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                useCaseCallback.onSuccess(response);
            }
        });
    }

    @Override
    public <V extends UseCase.ResponseValue> void onError(
            final UseCase.UseCaseCallback<V> useCaseCallback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                useCaseCallback.onError();
            }
        });
    }


    /**
     * Lazy loads an instance of the {@link ThreadPoolExecutor}.
     *
     * We will use this method for referencing the executor because it will allow us to
     * stop active and pending executions. Calling {@link ThreadPoolExecutor#shutdownNow()}
     * will prevent that instance from being able to execute any further requests, so we can
     * effectively set the instance to null so that this method will create a new instance.
     *
     * @return {@link ThreadPoolExecutor}
     */
    private static synchronized ThreadPoolExecutor getExecutor() {
        if (mThreadPoolExecutor == null) {
            mThreadPoolExecutor = new ThreadPoolExecutor(POOL_SIZE, MAX_POOL_SIZE, TIMEOUT,
                    TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(POOL_SIZE));
        }
        return mThreadPoolExecutor;
    }
}