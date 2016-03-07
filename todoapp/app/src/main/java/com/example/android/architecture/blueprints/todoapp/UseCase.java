package com.example.android.architecture.blueprints.todoapp;

public abstract class UseCase<T extends UseCase.RequestValues, R extends UseCase.ResponseValue>
         {

    private T mRequestValues;
    private UseCaseCallback<R> mUseCaseCallback;


    public void setRequestValues(T requestValues) {
        mRequestValues = requestValues;
    }

    public T getRequestValues() {
        return mRequestValues;
    }

    public UseCaseCallback<R> getUseCaseCallback() {
        return mUseCaseCallback;
    }

    public void setUseCaseCallback(
            UseCaseCallback<R> useCaseCallback) {
        mUseCaseCallback = useCaseCallback;
    }

    void run() {
       executeUseCase(mRequestValues);
    }

    protected abstract void executeUseCase(T requestValues);

    public static class RequestValues {
    }

    public class ResponseValue {

    }

    public interface UseCaseCallback<R> {
        void onSuccess(R response);
        void onError(Error error);
    }

}
