/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.testing.notes.util;

import android.os.SystemClock;
import android.support.test.espresso.IdlingResource;
import android.text.TextUtils;
import android.util.Log;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * An implementation of {@link IdlingResource} that determines idleness by maintaining an internal
 * counter. When the counter is 0 - it is considered to be idle, when it is non-zero it is not
 * idle. This is very similar to the way a {@link java.util.concurrent.Semaphore} behaves.
 * <p>
 * The counter may be incremented or decremented from any thread. If it reaches an illogical state
 * (like counter less than zero) it will throw an IllegalStateException.
 * </p>
 * <p>
 * This class can then be used to wrap up operations that while in progress should block tests from
 * accessing the UI.
 * </p>
 *
 * <pre>
 * {@code
 *   public interface FooServer {
 *     public Foo newFoo();
 *     public void updateFoo(Foo foo);
 *   }
 *
 *   public DecoratedFooServer implements FooServer {
 *     private final FooServer realFooServer;
 *     private final CountingIdlingResource fooServerIdlingResource;
 *
 *     public DecoratedFooServer(FooServer realFooServer,
 *         CountingIdlingResource fooServerIdlingResource) {
 *       this.realFooServer = checkNotNull(realFooServer);
 *       this.fooServerIdlingResource = checkNotNull(fooServerIdlingResource);
 *     }
 *
 *     public Foo newFoo() {
 *       fooServerIdlingResource.increment();
 *       try {
 *         return realFooServer.newFoo();
 *       } finally {
 *         fooServerIdlingResource.decrement();
 *       }
 *     }
 *
 *     public void updateFoo(Foo foo) {
 *       fooServerIdlingResource.increment();
 *       try {
 *         realFooServer.updateFoo(foo);
 *       } finally {
 *         fooServerIdlingResource.decrement();
 *       }
 *     }
 *   }
 *   }
 *   </pre>
 *
 * Then in your test setup:
 * <pre>
 *   {@code
 *     public void setUp() throws Exception {
 *       super.setUp();
 *       FooServer realServer = FooApplication.getFooServer();
 *       CountingIdlingResource countingResource = new CountingIdlingResource("FooServerCalls");
 *       FooApplication.setFooServer(new DecoratedFooServer(realServer, countingResource));
 *       Espresso.registerIdlingResource(countingResource);
 *     }
 *   }
 *   </pre>
 */
@SuppressWarnings("javadoc")
public final class CountingIdlingResource implements IdlingResource {

    private static final String TAG = "CountingIdlingResource";

    private final String resourceName;

    private final AtomicInteger counter = new AtomicInteger(0);

    private final boolean debugCounting;

    // written from main thread, read from any thread.
    private volatile ResourceCallback resourceCallback;

    // read/written from any thread - used for debugging messages.
    private volatile long becameBusyAt = 0;

    private volatile long becameIdleAt = 0;

    /**
     * Creates a CountingIdlingResource without debug tracing.
     *
     * @param resourceName the resource name this resource should report to Espresso.
     */
    public CountingIdlingResource(String resourceName) {
        this(resourceName, false);
    }

    /**
     * Creates a CountingIdlingResource.
     *
     * @param resourceName  the resource name this resource should report to Espresso.
     * @param debugCounting if true increment & decrement calls will print trace information to
     *                      logs.
     */
    public CountingIdlingResource(String resourceName, boolean debugCounting) {
        if (TextUtils.isEmpty(resourceName)) {
            throw new IllegalArgumentException("Resource name must not be empty or null");
        }
        this.resourceName = resourceName;
        this.debugCounting = debugCounting;
    }

    @Override
    public String getName() {
        return resourceName;
    }

    @Override
    public boolean isIdleNow() {
        return counter.get() == 0;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }

    /**
     * Increments the count of in-flight transactions to the resource being monitored.
     *
     * This method can be called from any thread.
     */
    public void increment() {
        int counterVal = counter.getAndIncrement();
        if (0 == counterVal) {
            becameBusyAt = SystemClock.uptimeMillis();
        }
        if (debugCounting) {
            Log.i(TAG, "Resource: " + resourceName + " in-use-count incremented to: " + (counterVal
                    + 1));
        }
    }

    /**
     * Decrements the count of in-flight transactions to the resource being monitored.
     *
     * If this operation results in the counter falling below 0 - an exception is raised.
     *
     * @throws IllegalStateException if the counter is below 0.
     */
    public void decrement() {
        int counterVal = counter.decrementAndGet();
        if (counterVal == 0) {
            // we've gone from non-zero to zero. That means we're idle now! Tell espresso.
            if (null != resourceCallback) {
                resourceCallback.onTransitionToIdle();
            }
            becameIdleAt = SystemClock.uptimeMillis();
        }
        if (debugCounting) {
            if (counterVal == 0) {
                Log.i(TAG, "Resource: " + resourceName + " went idle! (Time spent not idle: " +
                        (becameIdleAt - becameBusyAt) + ")");
            } else {
                Log.i(TAG, "Resource: " + resourceName + " in-use-count decremented to: "
                        + counterVal);
            }
        }
        if (counterVal < 0) {
            throw new IllegalArgumentException("Counter has been corrupted!");
        }
    }

    /**
     * Prints the current state of this resource to the logcat at info level.
     */
    public void dumpStateToLogs() {
        StringBuilder message = new StringBuilder("Resource: ")
                .append(resourceName)
                .append(" inflight transaction count: ")
                .append(counter.get());
        if (0 == becameBusyAt) {
            Log.i(TAG, message.append(" and has never been busy!").toString());
        } else {
            message.append(" and was last busy at: ")
                    .append(becameBusyAt);
            if (0 == becameIdleAt) {
                Log.w(TAG, message.append(" AND NEVER WENT IDLE!").toString());
            } else {
                message.append(" and last went idle at: ")
                        .append(becameIdleAt);
                Log.i(TAG, message.toString());
            }
        }
    }
}