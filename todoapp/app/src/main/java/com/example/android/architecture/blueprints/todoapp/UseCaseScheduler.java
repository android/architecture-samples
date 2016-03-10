package com.example.android.architecture.blueprints.todoapp;

public interface UseCaseScheduler {
    void execute(Runnable runnable);

    <R extends UseCase.ResponseValue> void notifyResponse(final R response,
            final UseCase.UseCaseCallback<R> useCaseCallback);

    <R extends UseCase.ResponseValue> void onError(final Error error,
            final UseCase.UseCaseCallback<R> useCaseCallback);
}
