package com.example.android.architecture.blueprints.todoapp;


import android.os.Handler;

import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource;

public class UseCaseHandler {

    private final UseCaseScheduler mUseCaseScheduler;
    private final Handler mHandler = new Handler();

    public UseCaseHandler(UseCaseScheduler useCaseScheduler) {
        this.mUseCaseScheduler = useCaseScheduler;
    }

    public <T extends UseCase.RequestValues, R extends UseCase.ResponseValue> void execute(
            final UseCase<T, R> useCase,
            T values,
            UseCase.UseCaseCallback<R> callback) {
        useCase.setRequestValues(values);
        useCase.setUseCaseCallback(new UiCallbackWrapper(callback, this));
        mUseCaseScheduler.execute(new Runnable() {
            @Override
            public void run() {
                // The network request might be handled in a different thread so make sure
                // Espresso knows
                // that the app is busy until the response is handled.
                EspressoIdlingResource.increment(); // App is busy until further notice

                useCase.run();

                // This callback may be called twice, once for the cache and once for loading
                // the data from the server API, so we check before decrementing, otherwise
                // it throws "Counter has been corrupted!" exception.
                if (!EspressoIdlingResource.getIdlingResource().isIdleNow()) {
                    EspressoIdlingResource.decrement(); // Set app as idle.
                }
            }
        });
    }

    public <R extends UseCase.ResponseValue> void notifyResponse(final R response,
            final UseCase.UseCaseCallback<R> useCaseCallback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                useCaseCallback.onSuccess(response);
            }
        });
    }

    private <R extends UseCase.ResponseValue> void notifyError(final Error error,
            final UseCase.UseCaseCallback<R> useCaseCallback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                useCaseCallback.onError(error);
            }
        });
    }

    private class UiCallbackWrapper<R extends UseCase.ResponseValue> implements
            UseCase.UseCaseCallback<R> {
        private final UseCase.UseCaseCallback<R> mCallback;
        private final UseCaseHandler mUseCaseHandler;

        public UiCallbackWrapper(
                UseCase.UseCaseCallback<R> callback,
                UseCaseHandler useCaseHandler) {
            mCallback = callback;
            mUseCaseHandler = useCaseHandler;
        }

        @Override
        public void onSuccess(R response) {
            mUseCaseHandler.notifyResponse(response, mCallback);
        }

        @Override
        public void onError(Error error) {
            mUseCaseHandler.notifyError(error, mCallback);
        }
    }
}
